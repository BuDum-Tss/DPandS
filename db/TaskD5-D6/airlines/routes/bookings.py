import string
from datetime import datetime
import random

from fastapi import APIRouter
from sqlmodel import Session, select

from airlines.dependencies import engine
from airlines.tables import TicketBase, FareConditions, Seat, Aircraft, Flight, BoardingPass, TicketFlight, Booking, \
    Ticket
import sqlalchemy as sa

router = APIRouter()


class BookingCreate(TicketBase):
    book_date: datetime
    fare_conditions: FareConditions
    route: list[int]


def calculate_amount(flight: Flight, fare_conditions: FareConditions, session) -> float:
    tickets = session.query(TicketFlight).filter(TicketFlight.flight_id == flight.flight_id,
                                                 TicketFlight.fare_conditions == fare_conditions).all()
    if len(tickets) > 0:
        return tickets[0].amount
    else:
        return 0


def is_available(flight: Flight, fare_conditions: FareConditions, session) -> bool:
    seats = session.query(Seat).filter(Seat.aircraft_code == flight.aircraft_code,
                                       Seat.fare_conditions == fare_conditions).all()
    tickets = session.query(TicketFlight).filter(TicketFlight.flight_id == flight.flight_id,
                                                 TicketFlight.fare_conditions == fare_conditions).all()
    return len(seats) > len(tickets)


def generate_ref(session):
    existing_refs = {booking.book_ref for booking in session.exec(select(Booking)).all()}
    while True:
        new_ref = ''.join(random.choices(string.hexdigits.upper(), k=6))
        if new_ref not in existing_refs:
            return new_ref


def generate_no(session):
    existing_nos = {ticket.ticket_no for ticket in session.exec(select(Ticket)).all()}
    while True:
        new_ref = ''.join(random.choices(string.digits.upper(), k=13))
        if new_ref not in existing_nos:
            return new_ref


@router.post('/', response_model=str)
def create_booking(booking: BookingCreate):
    with Session(engine) as session:
        amount: list[float] = [0.0]
        flights = []
        for flight_id in booking.route:
            flight: Flight = session.exec(select(Flight).where(Flight.flight_id == flight_id)).one()
            flights.append(flight)
            if is_available(flight, booking.fare_conditions, session):
                amount.append(calculate_amount(flight, booking.fare_conditions, session))
            else:
                return f"No available seats in flight {flight}"
        print(amount)
        db_booking = Booking(book_ref=generate_ref(session),
                             book_date=booking.book_date,
                             total_amount=sum(amount))
        session.add(db_booking)

        db_ticket = Ticket(ticket_no=generate_no(session),
                           book_ref=db_booking.book_ref,
                           passenger_id=booking.passenger_id,
                           passenger_name=booking.passenger_name,
                           contact_data=booking.contact_data)
        session.add(db_ticket)

        for i, flight in enumerate(flights):
            db_ticket_flight = TicketFlight(ticket_no=db_ticket.ticket_no,
                                            flight_id=flight.flight_id,
                                            fare_conditions=booking.fare_conditions,
                                            amount=amount[i])
            session.add(db_ticket_flight)
        session.commit()
        #session.refresh(db_booking)
        session.refresh(db_ticket)
        #session.refresh(db_ticket_flight)

        return db_ticket.ticket_no
