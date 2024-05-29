from datetime import datetime, date

from fastapi import APIRouter, Header
from sqlmodel import Session, select

from airlines.api import Locale
from airlines.dependencies import engine, ALModel
from airlines.tables import Airport, Flight, FlightData, Seat, BoardingPass, FareConditions, TicketFlight, FlightPrice

router = APIRouter()

from sqlalchemy import func, and_


def has_available_seats(flight: Flight, fare_conditions: FareConditions, session) -> bool:
    boarding_passes: BoardingPass = session.exec(
        select(BoardingPass).where(BoardingPass.flight_id == flight.flight_id)).all()
    occupied_seats = [boarding_pass.seat_no for boarding_pass in boarding_passes]
    seats: Seat = session.query(Seat).filter(Seat.aircraft_code == flight.aircraft_code,
                                             Seat.seat_no.not_in(occupied_seats),
                                             Seat.fare_conditions == fare_conditions).all()
    return seats is not None


def get_flights_by_time(city: str, time: datetime, fare_conditions: FareConditions,
                        session: Session, locale: Locale) -> list[Flight]:
    db_airports = session.exec(
        select(Airport)
        .where(func.jsonb_extract_path_text(Airport.city, locale.value) == city)).all()
    airports_codes = [airports.airport_code for airports in db_airports]
    subquery = session.query(
        Flight.arrival_airport,
        func.min(Flight.scheduled_departure).label('min_scheduled_departure')
    ).filter(
        Flight.departure_airport.in_(airports_codes),
        Flight.scheduled_departure > time
    ).group_by(Flight.arrival_airport).subquery()
    # Выполняем основной запрос, используя субзапрос для выбора минимального времени отправления
    db_flights = session.query(Flight).join(
        subquery,
        and_(
            Flight.arrival_airport == subquery.c.arrival_airport,
            Flight.scheduled_departure == subquery.c.min_scheduled_departure
        )
    ).all()
    print(f"{city} - {time}")
    print(db_flights)
    return [flight for flight in db_flights if has_available_seats(flight, fare_conditions, session)]


def flight_to_data(flight: Flight, session: Session, locale: Locale) -> FlightData:
    departure_airport: Airport = session.exec(
        select(Airport).where(Airport.airport_code == flight.departure_airport)).one()
    arrival_airport: Airport = session.exec(
        select(Airport).where(Airport.airport_code == flight.arrival_airport)).one()
    db_flight_data = FlightData(flight_id=flight.flight_id,
                                departure_airport=departure_airport.city[locale.value],
                                scheduled_departure=flight.scheduled_departure,
                                arrival_airport=arrival_airport.city[locale.value],
                                scheduled_arrival=flight.scheduled_arrival)
    return db_flight_data


def do_step(curr_city: str, passed_cities: list[str], time: datetime, destination_city: str, n: int,
            fare_conditions: FareConditions,
            session: Session, locale: Locale) -> list[list[FlightData]]:
    print(f"City: {curr_city},   passed: {passed_cities}")
    if curr_city == destination_city:
        return [[]]
    if n == 0 or curr_city in passed_cities:
        return []
    passed_cities.append(curr_city)
    db_flights = get_flights_by_time(curr_city, time, fare_conditions, session, locale)
    result = []
    # return result
    for db_flight in db_flights:
        next_db_airport: Airport = session.exec(
            select(Airport).where(Airport.airport_code == db_flight.arrival_airport)).one()
        if next_db_airport not in passed_cities:
            routes: list[list[FlightData]] = do_step(next_db_airport.city[locale.value],
                                                     passed_cities, db_flight.scheduled_arrival, destination_city,
                                                     n - 1, fare_conditions,
                                                     session, locale)
            db_flight_data = flight_to_data(db_flight, session, locale)
            for i, route in enumerate(routes):
                routes[i] = [db_flight_data] + route
            result += routes
    return result


@router.get("", response_model=list[list[FlightData]])
def find_routes(origin: str,
                destination: str,
                departure_date: date,
                fare_conditions: FareConditions,
                transfers: int = 100,
                locale: Locale = Header(Locale.en)):
    with Session(engine) as session:
        return do_step(origin, [], departure_date, destination, transfers + 1, fare_conditions, session, locale)


def calculate_amount(flight: Flight, fare_conditions: FareConditions, session) -> float:
    tickets = session.query(TicketFlight).filter(TicketFlight.flight_id == flight.flight_id,
                                                 TicketFlight.fare_conditions == fare_conditions).all()
    if len(tickets) > 0:
        return tickets[0].amount
    else:
        return 0


@router.post("/sales/update", response_model=list[FlightPrice])
def update_flight_sales():
    with Session(engine) as session:
        price_list: list[FlightPrice] = []
        db_flights: list[Flight] = session.exec(select(Flight)).all()
        for flight in db_flights:
            for fare_conditions in FareConditions:
                try:
                    price = calculate_amount(flight, fare_conditions, session)
                except Exception as e:
                    print(f"asd: {e}")
                flight_price = FlightPrice(price_id=f"{flight.flight_id}-{fare_conditions.value}",
                                           flight_id=flight.flight_id,
                                           fare_conditions=fare_conditions,
                                           price=price)
                session.add(flight_price)
                price_list.append(flight_price)
        session.commit()
        return price_list
