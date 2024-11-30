from contextlib import asynccontextmanager
from enum import Enum
from typing import AsyncIterator

from fastapi import FastAPI, Request, Header, Query, APIRouter, Depends
from sqlalchemy import func
from sqlmodel import select, Session

from airlines.api import Locale
from airlines.dependencies import engine, ALModel, get_session
from airlines.tables import Airport, Flight

router = APIRouter()


@router.get("", response_model=list[str])
def get_cities(locale: Locale = Header(Locale.en)) -> list[str]:
    with Session(engine) as session:
        db_airports = session.exec(select(Airport)).all()
        cities = [airport.city[locale.value] for airport in db_airports]
        return cities


@router.get("/{name}/airports", response_model=list[str])
def get_cities_airports(name: str,
                        locale: Locale = Header(Locale.en)) -> list[str]:
    with Session(engine) as session:
        db_airports = session.exec(
            select(Airport)
            .where(func.jsonb_extract_path_text(Airport.city, locale.value) == name)
        ).all()
        cities = [airport.airport_name[locale.value] for airport in db_airports]
        return cities
