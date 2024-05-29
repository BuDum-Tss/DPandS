from datetime import datetime
from enum import Enum
from typing import Optional
from sqlmodel import Field

from airlines.dependencies import ALModel


class Booking(ALModel, table=True):
    __tablename__ = 'bookings'
    book_ref: Optional[str] = Field(primary_key=True,
                                    regex=r'^[0-9A-F]{6}$',
                                    min_length=6,
                                    max_length=6,
                                    unique=True,
                                    schema_extra={'example': "12345A"})
    book_date: datetime = Field(regex=r'^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}.\d{6} [+-]\d{2}:\d{2}$',
                                nullable=False,
                                schema_extra={'example': "2000-01-01 00:00:00.000000 +00:00"})
    total_amount: float = Field(nullable=False,
                                schema_extra={'example': "10000.00"})


class TicketBase(ALModel):
    passenger_id: str
    passenger_name: str
    contact_data: str


class Ticket(TicketBase, table=True):
    __tablename__ = 'tickets'
    ticket_no: str = Field(primary_key=True,
                           regex=r'^[0-9]{13}$',
                           min_length=13,
                           max_length=13,
                           nullable=False,
                           unique=True,
                           schema_extra={'example': "0000000000001"})
    book_ref: str = Field(foreign_key="bookings.book_ref")
    passenger_id: str = Field(regex='^[0-9]{4} [0-9]{6}$',
                              max_length=20,
                              nullable=False,
                              unique=True,
                              schema_extra={'example': "0000 000000"})
    passenger_name: str = Field(regex='^[A-Z]* [A-Z]*$',
                                nullable=False,
                                schema_extra={'example': "NAME SURNAME"})
    contact_data: str = Field(nullable=False,
                              schema_extra={
                                  'example': "{\"email\": \"some@mail.ru\", \"phone\": \"+88005553535\"}"})


class FareConditions(Enum):
    Economy = 'Economy'
    Comfort = 'Comfort'
    Business = 'Business'


class TicketFlight(ALModel, table=True):
    __tablename__ = 'ticket_flights'
    ticket_no: str = Field(foreign_key="tickets.ticket_no")
    flight_id: int = Field(primary_key=True,
                           foreign_key="flights.flight_id")
    fare_conditions: FareConditions = Field(nullable=False,
                                            schema_extra={'example': "Comfort"})
    amount: float = Field(nullable=False,
                          schema_extra={'example': "10000.00"})


class BoardingPass(ALModel, table=True):
    __tablename__ = 'boarding_passes'
    ticket_no: str = Field(primary_key=True,
                           foreign_key="tickets.ticket_no")
    flight_id: int = Field(primary_key=True,
                           foreign_key="flights.flight_id")
    boarding_no: int = Field(nullable=False,
                             schema_extra={'example': "10"})
    seat_no: str = Field(foreign_key="seats.seat_no")


class Status(Enum):
    Delayed = 'Delayed'
    Departed = 'Departed'
    On_Time = 'On Time'
    Arrived = 'Arrived'
    Cancelled = 'Cancelled'
    Scheduled = 'Scheduled'

    def __str__(self):
        return self.value


class FlightData(ALModel):
    flight_id: int
    departure_airport: str
    scheduled_departure: datetime
    arrival_airport: str
    scheduled_arrival: datetime


class Flight(ALModel, table=True):
    __tablename__ = 'flights'
    flight_id: int = Field(primary_key=True,
                           foreign_key="flights.flight_id")
    flight_no: str = Field(regex='^[A-Z]{2}[0-9]{4}$',
                           max_length=6,
                           nullable=False,
                           schema_extra={'example': "PG001"})
    scheduled_departure: datetime = Field(regex=r'^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}.\d{6} [+-]\d{2}:\d{2}$',
                                          nullable=False,
                                          schema_extra={'example': "2000-01-01 00:00:00.000000 +00:00"})
    scheduled_arrival: datetime = Field(regex=r'^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}.\d{6} [+-]\d{2}:\d{2}$',
                                        nullable=False,
                                        schema_extra={'example': "2000-01-01 00:00:00.000000 +00:00"})
    departure_airport: str = Field(foreign_key='airports_data.airport_code')
    arrival_airport: str = Field(foreign_key='airports_data.airport_code')
    status: str = Field(nullable=False,
                        schema_extra={'example': "Delayed"})
    aircraft_code: str = Field(foreign_key="aircrafts_data.aircraft_code")
    actual_departure: datetime = Field(regex=r'^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}.\d{6} [+-]\d{2}:\d{2}$',
                                       nullable=True,
                                       schema_extra={'example': "2000-01-01 00:00:00.000000 +00:00"})
    actual_arrival: datetime = Field(regex=r'^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}.\d{6} [+-]\d{2}:\d{2}$',
                                     nullable=True,
                                     schema_extra={'example': "2000-01-01 00:00:00.000000 +00:00"})


class FlightPrice(ALModel, table=True):
    __tablename__ = 'flights_prices'
    price_id: str = Field(primary_key=True,
                          regex=r'^[0-9A-F]{6}$',
                          unique=True,
                          schema_extra={'example': "SU200_Comfort"})
    flight_id: int = Field(foreign_key="flights.flight_id")
    fare_conditions: FareConditions = Field(nullable=False,
                                            schema_extra={'example': "Comfort"})
    price: float = Field(nullable=False,
                         schema_extra={'example': "10000.00"})


class Aircraft(ALModel, table=True):
    __tablename__ = 'aircrafts_data'
    aircraft_code: str = Field(primary_key=True,
                               regex=r'^[A-Z0-9]{3}$',
                               nullable=False,
                               schema_extra={'example': "SU9"})
    model: str = Field(nullable=False,
                       schema_extra={'example': "{\"en\": \"Boeing 777-300\", \"ru\": \"Боинг 777-300\"}"})
    range: int = Field(nullable=False,
                       schema_extra={'example': "11100"})


class Seat(ALModel, table=True):
    __tablename__ = 'seats'
    aircraft_code: str = Field(foreign_key="aircrafts_data.aircraft_code")
    seat_no: str = Field(primary_key=True,
                         regex='^[0-9]*[A-Z]$',
                         max_length=4,
                         nullable=False,
                         schema_extra={'example': "1A"})
    fare_conditions: FareConditions = Field(nullable=False,
                                            schema_extra={'example': "Comfort"})


class Airport(ALModel, table=True):
    __tablename__ = 'airports_data'
    airport_code: str = Field(primary_key=True,
                              regex=r'^[A-Z]{3}$',
                              nullable=False,
                              schema_extra={'example': "YKS"})
    airport_name: str = Field(nullable=False,
                              schema_extra={'example': "{\"en\": \"Yakutsk Airport\", \"ru\": \"Якутск\"}"})
    city: str = Field(nullable=False,
                      schema_extra={'example': "{\"en\": \"Yakutsk\", \"ru\": \"Якутск\"}"})
    coordinates: str = Field(nullable=False,
                             schema_extra={'example': "(129.77099609375,62.093299865722656)"})
    timezone: str = Field(regex=r'^[A-Za-z]*/[A-Za-z]*$',
                          nullable=False,
                          schema_extra={'example': "Asia/Yakutsk"})
