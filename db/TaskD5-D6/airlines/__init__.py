import uvicorn

from airlines.api import airlines_app


def entrypoint():
    uvicorn.run("airlines.api:airlines_app", host="localhost", port=8000, reload=True)
