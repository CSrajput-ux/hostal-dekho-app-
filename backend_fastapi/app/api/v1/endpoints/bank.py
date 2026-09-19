from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app import models, schemas
from app.api.deps import get_db, get_current_user, require_roles

router = APIRouter()

@router.post("/", response_model=schemas.BankAccountResponse)
def add_bank_account(
    account: schemas.BankAccountCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(require_roles(models.UserRole.OWNER, models.UserRole.ADMIN)),
):
    # Set all other accounts to not primary if this one is primary
    if account.is_primary:
        db.query(models.BankAccount).filter(models.BankAccount.user_id == current_user.id).update({"is_primary": False})
        
    new_account = models.BankAccount(
        user_id=current_user.id,
        account_holder_name=account.account_holder_name,
        account_number=account.account_number,
        ifsc_code=account.ifsc_code,
        bank_name=account.bank_name,
        is_primary=account.is_primary
    )
    db.add(new_account)
    db.commit()
    db.refresh(new_account)
    return new_account


@router.get("/my", response_model=List[schemas.BankAccountResponse])
def get_my_bank_accounts(
    db: Session = Depends(get_db),
    current_user: models.User = Depends(require_roles(models.UserRole.OWNER, models.UserRole.ADMIN)),
):
    accounts = db.query(models.BankAccount).filter(models.BankAccount.user_id == current_user.id).all()
    return accounts
