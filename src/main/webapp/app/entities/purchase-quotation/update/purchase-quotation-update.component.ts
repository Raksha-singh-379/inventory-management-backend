import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';

import { IPurchaseQuotation, PurchaseQuotation } from '../purchase-quotation.model';
import { PurchaseQuotationService } from '../service/purchase-quotation.service';
import { ISecurityUser } from 'app/entities/security-user/security-user.model';
import { SecurityUserService } from 'app/entities/security-user/service/security-user.service';
import { OrderType } from 'app/entities/enumerations/order-type.model';
import { Status } from 'app/entities/enumerations/status.model';

@Component({
  selector: 'jhi-purchase-quotation-update',
  templateUrl: './purchase-quotation-update.component.html',
})
export class PurchaseQuotationUpdateComponent implements OnInit {
  isSaving = false;
  orderTypeValues = Object.keys(OrderType);
  statusValues = Object.keys(Status);

  securityUsersSharedCollection: ISecurityUser[] = [];

  editForm = this.fb.group({
    id: [],
    totalPOAmount: [],
    totalGSTAmount: [],
    expectedDeliveryDate: [],
    poDate: [],
    orderType: [],
    orderStatus: [],
    clientName: [],
    clientMobile: [],
    clientEmail: [],
    termsAndCondition: [],
    notes: [],
    lastModified: [null, [Validators.required]],
    lastModifiedBy: [null, [Validators.required]],
    freeField1: [],
    freeField2: [],
    securityUser: [],
  });

  constructor(
    protected purchaseQuotationService: PurchaseQuotationService,
    protected securityUserService: SecurityUserService,
    protected activatedRoute: ActivatedRoute,
    protected fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ purchaseQuotation }) => {
      if (purchaseQuotation.id === undefined) {
        const today = dayjs().startOf('day');
        purchaseQuotation.expectedDeliveryDate = today;
        purchaseQuotation.poDate = today;
      }

      this.updateForm(purchaseQuotation);

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const purchaseQuotation = this.createFromForm();
    if (purchaseQuotation.id !== undefined) {
      this.subscribeToSaveResponse(this.purchaseQuotationService.update(purchaseQuotation));
    } else {
      this.subscribeToSaveResponse(this.purchaseQuotationService.create(purchaseQuotation));
    }
  }

  trackSecurityUserById(index: number, item: ISecurityUser): number {
    return item.id!;
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IPurchaseQuotation>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(purchaseQuotation: IPurchaseQuotation): void {
    this.editForm.patchValue({
      id: purchaseQuotation.id,
      totalPOAmount: purchaseQuotation.totalPOAmount,
      totalGSTAmount: purchaseQuotation.totalGSTAmount,
      expectedDeliveryDate: purchaseQuotation.expectedDeliveryDate ? purchaseQuotation.expectedDeliveryDate.format(DATE_TIME_FORMAT) : null,
      poDate: purchaseQuotation.poDate ? purchaseQuotation.poDate.format(DATE_TIME_FORMAT) : null,
      orderType: purchaseQuotation.orderType,
      orderStatus: purchaseQuotation.orderStatus,
      clientName: purchaseQuotation.clientName,
      clientMobile: purchaseQuotation.clientMobile,
      clientEmail: purchaseQuotation.clientEmail,
      termsAndCondition: purchaseQuotation.termsAndCondition,
      notes: purchaseQuotation.notes,
      lastModified: purchaseQuotation.lastModified,
      lastModifiedBy: purchaseQuotation.lastModifiedBy,
      freeField1: purchaseQuotation.freeField1,
      freeField2: purchaseQuotation.freeField2,
      securityUser: purchaseQuotation.securityUser,
    });

    this.securityUsersSharedCollection = this.securityUserService.addSecurityUserToCollectionIfMissing(
      this.securityUsersSharedCollection,
      purchaseQuotation.securityUser
    );
  }

  protected loadRelationshipsOptions(): void {
    this.securityUserService
      .query()
      .pipe(map((res: HttpResponse<ISecurityUser[]>) => res.body ?? []))
      .pipe(
        map((securityUsers: ISecurityUser[]) =>
          this.securityUserService.addSecurityUserToCollectionIfMissing(securityUsers, this.editForm.get('securityUser')!.value)
        )
      )
      .subscribe((securityUsers: ISecurityUser[]) => (this.securityUsersSharedCollection = securityUsers));
  }

  protected createFromForm(): IPurchaseQuotation {
    return {
      ...new PurchaseQuotation(),
      id: this.editForm.get(['id'])!.value,
      totalPOAmount: this.editForm.get(['totalPOAmount'])!.value,
      totalGSTAmount: this.editForm.get(['totalGSTAmount'])!.value,
      expectedDeliveryDate: this.editForm.get(['expectedDeliveryDate'])!.value
        ? dayjs(this.editForm.get(['expectedDeliveryDate'])!.value, DATE_TIME_FORMAT)
        : undefined,
      poDate: this.editForm.get(['poDate'])!.value ? dayjs(this.editForm.get(['poDate'])!.value, DATE_TIME_FORMAT) : undefined,
      orderType: this.editForm.get(['orderType'])!.value,
      orderStatus: this.editForm.get(['orderStatus'])!.value,
      clientName: this.editForm.get(['clientName'])!.value,
      clientMobile: this.editForm.get(['clientMobile'])!.value,
      clientEmail: this.editForm.get(['clientEmail'])!.value,
      termsAndCondition: this.editForm.get(['termsAndCondition'])!.value,
      notes: this.editForm.get(['notes'])!.value,
      lastModified: this.editForm.get(['lastModified'])!.value,
      lastModifiedBy: this.editForm.get(['lastModifiedBy'])!.value,
      freeField1: this.editForm.get(['freeField1'])!.value,
      freeField2: this.editForm.get(['freeField2'])!.value,
      securityUser: this.editForm.get(['securityUser'])!.value,
    };
  }
}
