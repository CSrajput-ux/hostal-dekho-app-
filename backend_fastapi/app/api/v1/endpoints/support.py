from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app import models, schemas
from app.api.deps import get_db, get_current_user

router = APIRouter()

@router.post("/", response_model=schemas.SupportTicketResponse)
def create_ticket(
    ticket: schemas.SupportTicketCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user),
):
    new_ticket = models.SupportTicket(
        user_id=current_user.id,
        subject=ticket.subject,
        description=ticket.description,
        status="OPEN"
    )
    db.add(new_ticket)
    db.commit()
    db.refresh(new_ticket)
    return new_ticket


@router.get("/my", response_model=List[schemas.SupportTicketResponse])
def get_my_tickets(
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user),
):
    tickets = db.query(models.SupportTicket).filter(models.SupportTicket.user_id == current_user.id).all()
    return tickets
