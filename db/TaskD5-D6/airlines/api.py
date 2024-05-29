from contextlib import asynccontextmanager
from enum import Enum
from typing import AsyncIterator

from fastapi import FastAPI, Depends
from airlines.dependencies import engine, ALModel, get_session


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncIterator[None]:
    ALModel.metadata.create_all(engine)
    yield


airlines_app = FastAPI(title="AirLines API", version="1.0.0", lifespan=lifespan)


class Locale(Enum):
    ru = "ru"
    en = "en"


@airlines_app.get("/health")
def health() -> str:
    print("health")
    return "Server is running!"


import airlines.routes.cities as cities_api

airlines_app.include_router(cities_api.router,
                            prefix="/cities",
                            tags=["cities"],
                            dependencies=[Depends(get_session)])

import airlines.routes.airports as airports_api

airlines_app.include_router(airports_api.router,
                            prefix="/airports",
                            tags=["airports"],
                            dependencies=[Depends(get_session)])

import airlines.routes.flights as flights_api

airlines_app.include_router(flights_api.router,
                            prefix="/flights",
                            tags=["flights"],
                            dependencies=[Depends(get_session)])

import airlines.routes.bookings as bookings_api

airlines_app.include_router(bookings_api.router,
                            prefix="/bookings",
                            tags=["bookings"],
                            dependencies=[Depends(get_session)])

import airlines.routes.checkins as checkins_api

airlines_app.include_router(checkins_api.router,
                            prefix="/checkins",
                            tags=["checkins"],
                            dependencies=[Depends(get_session)])


