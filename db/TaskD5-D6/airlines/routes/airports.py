from enum import Enum

from fastapi import Header, APIRouter
from sqlmodel import select, Session

from airlines.tables import Airport, Flight
from airlines.api import Locale

from airlines.dependencies import engine, ALModel

router = APIRouter()


@router.get("", response_model=list[str])
def get_cities(locale: Locale = Header(Locale.en)) -> list[str]:
    with Session(engine) as session:
        db_airports = session.exec(select(Airport)).all()
        airports = [airport.airport_name[locale.value] for airport in db_airports]
        return airports


class Type(Enum):
    inbound = "inbound"
    outbound = "outbound"


class Schedule(ALModel):
    days_of_week: list[int]
    time: str
    flight_no: str
    airport: str


@router.get("/{code}/schedules", response_model=list[Schedule])
def get_airports_schedules(code: str,
                           schedule_type: Type = Type.inbound,
                           locale: Locale = Header(Locale.en)) -> list[Schedule]:
    _get_airport = (lambda db_object:
                    db_object.departure_airport if schedule_type == Type.inbound else db_object.arrival_airport)
    _get_other_airport = (lambda db_object:
                          db_object.arrival_airport if schedule_type == Type.inbound else db_object.departure_airport)
    _get_timestamp = (lambda db_object:
                      db_object.scheduled_arrival if schedule_type == Type.inbound else db_object.scheduled_departure)

    def _add_schedule(_prev_flight, _result, _weekdays):
        db_airport = session.exec(select(Airport).
                                  where(Airport.airport_code == _get_other_airport(_prev_flight))).one()
        _result.append(Schedule(
            days_of_week=[day for day in _weekdays],
            time=str(_get_timestamp(_prev_flight).time()),
            flight_no=_prev_flight.flight_no,
            airport=db_airport.airport_name[locale.value],
        ))

    with Session(engine) as session:
        db_flights = (session
                      .query(Flight)
                      .filter(_get_airport(Flight) == code)
                      .order_by(Flight.flight_no, _get_other_airport(Flight))
                      .all())
        result: [Schedule] = []
        prev_flight: Flight = None
        weekdays: set = set()

        for db_flight in db_flights:
            print(db_flight)
            print(prev_flight is not None)
            if prev_flight is not None:
                print((_get_other_airport(db_flight) != _get_other_airport(prev_flight)
                       or db_flight.flight_no != prev_flight.flight_no
                       or _get_timestamp(db_flight).time() != _get_timestamp(prev_flight).time()))
            if (prev_flight is not None and
                    (_get_other_airport(db_flight) != _get_other_airport(prev_flight)
                     or db_flight.flight_no != prev_flight.flight_no
                     or _get_timestamp(db_flight).time() != _get_timestamp(prev_flight).time())):
                _add_schedule(prev_flight, result, weekdays)
                weekdays: set = set()
            weekdays: set = weekdays | {_get_timestamp(db_flight).weekday()}
            prev_flight = db_flight
        _add_schedule(prev_flight, result, weekdays)
        return result
