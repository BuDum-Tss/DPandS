from fastapi import APIRouter
from sqlmodel import Session, select

from airlines.dependencies import ALModel, engine
from airlines.tables import BoardingPass, Flight, Ticket, Seat, FareConditions, TicketFlight

router = APIRouter()


def get_available_seats(flight: Flight, fare_conditions: FareConditions, session) -> list[Seat]:
    boarding_passes: BoardingPass = session.exec(
        select(BoardingPass).where(BoardingPass.flight_id == flight.flight_id)).all()
    occupied_seats = [boarding_pass.seat_no for boarding_pass in boarding_passes]
    seats: Seat = session.query(Seat).filter(Seat.aircraft_code == flight.aircraft_code,
                                             Seat.seat_no.not_in(occupied_seats),
                                             Seat.fare_conditions == fare_conditions).all()
    return seats


def get_prev_boarding_no(flight_id: int, session) -> int:
    boardings = session.exec(select(BoardingPass).where(BoardingPass.flight_id == flight_id)).all()
    nos = [0] + [boardings.boarding_no for boardings in boardings]
    return max(nos)


class CheckInData(ALModel):
    flight_id: int
    ticket_no: str


@router.post('/', response_model=BoardingPass)
def check_in(data: CheckInData):
    with Session(engine) as session:
        flight: Flight = session.exec(select(Flight).where(Flight.flight_id == data.flight_id)).one()
        ticket_flight: TicketFlight = session.query(TicketFlight).filter(TicketFlight.flight_id == data.flight_id,
                                                                         TicketFlight.ticket_no == data.ticket_no).one()
        available_seats = get_available_seats(flight, ticket_flight.fare_conditions, session)
        if available_seats is not None and len(available_seats) > 0:
            bd_boarding_pass = BoardingPass(ticket_no=data.ticket_no,
                                            flight_id=data.flight_id,
                                            boarding_no=get_prev_boarding_no(flight.flight_id, session) + 1,
                                            seat_no=available_seats[0])
            return bd_boarding_pass
        else:
            return None
