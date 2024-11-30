from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlmodel import SQLModel

from airlines.settings import USERNAME, DB_ADDRESS

db_url = f"postgresql://{USERNAME}@{DB_ADDRESS}/demo"

engine = create_engine(db_url, echo=True)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


def get_session():
    session = SessionLocal()
    try:
        yield session
    finally:
        session.close()


class ALModel(SQLModel):
    pass
