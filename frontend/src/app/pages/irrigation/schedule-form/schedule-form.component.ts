import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { IrrigationService, ProgrammeArrosage } from '../../../services/irrigation.service';
import { NotificationService } from '../../../services/notification.service';

@Component({
  selector: 'app-schedule-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatDatepickerModule,
    MatNativeDateModule
  ],
  templateUrl: './schedule-form.component.html',
  styleUrls: ['./schedule-form.component.scss']
})
export class ScheduleFormComponent implements OnInit {
  form!: FormGroup;
  loading = false;
  submitting = false;
  isEditMode = false;
  scheduleId: number | null = null;
  // Align with backend status values (PLANIFIE is the planned state the executor expects)
  statuses = ['PLANIFIE', 'EXECUTED', 'FAILED', 'CANCELLED', 'REPLANIFIE'];

  constructor(
    private fb: FormBuilder,
    private irrigationService: IrrigationService,
    private notificationService: NotificationService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.initializeForm();
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        const parsedId = Number(params['id']);
        if (!Number.isNaN(parsedId)) {
          this.scheduleId = parsedId;
          this.loadSchedule(parsedId);
        }
      }
    });
  }

  initializeForm(): void {
    this.form = this.fb.group({
      parcelleId: ['', [Validators.required, Validators.pattern(/^[0-9]+$/)]],
      scheduledDate: ['', Validators.required],
      scheduledTime: ['12:00', Validators.required],
      duree: ['', [Validators.required, Validators.pattern(/^[0-9]+$/)]],
      volumePrevu: ['', [Validators.required, Validators.pattern(/^[0-9]+(\.[0-9]+)?$/)]],
      statut: ['PLANIFIE', Validators.required]
    });
  }

  private combineDateAndTime(): string {
    const date = this.form.get('scheduledDate')?.value;
    const time = this.form.get('scheduledTime')?.value;
    if (!date || !time) return '';
    
    const dateStr = date instanceof Date 
      ? date.toISOString().split('T')[0]
      : new Date(date).toISOString().split('T')[0];
    
    return `${dateStr}T${time}:00`;
  }

  private splitDateAndTime(dateTime: string): void {
    if (!dateTime) return;
    const [date, time] = dateTime.split('T');
    const timeOnly = time?.substring(0, 5) || '12:00';
    this.form.patchValue({
      scheduledDate: new Date(date + 'T00:00:00'),
      scheduledTime: timeOnly
    });
  }

  loadSchedule(id: number): void {
    this.loading = true;
    console.log('Loading schedule ID:', id);
    this.irrigationService.getProgramme(id).subscribe({
      next: (schedule) => {
        console.log('Schedule loaded:', schedule);
        this.splitDateAndTime(schedule.datePlanifiee);
        this.form.patchValue({
          parcelleId: schedule.parcelleId,
          duree: schedule.duree,
          volumePrevu: schedule.volumePrevu,
          statut: schedule.statut
        });
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading schedule:', error);
        this.loading = false;
        this.notificationService.error('Failed to load schedule: ' + (error?.status || 'Unknown error'));
      }
    });
  }

  submit(): void {
    if (!this.form.valid) {
      this.notificationService.warning('Please fill in all required fields correctly');
      return;
    }

    this.submitting = true;
    const formData: ProgrammeArrosage = {
      ...this.form.value,
      datePlanifiee: this.combineDateAndTime()
    };
    delete (formData as any).scheduledDate;
    delete (formData as any).scheduledTime;

    const request = this.isEditMode
      ? this.irrigationService.updateProgramme(this.scheduleId!, formData)
      : this.irrigationService.createProgramme(formData);

    request.subscribe({
      next: () => {
        this.submitting = false;
        const message = this.isEditMode ? 'Schedule updated successfully' : 'Schedule created successfully';
        this.notificationService.success(message);
        this.router.navigate(['/irrigation/schedules']);
      },
      error: (error) => {
        console.error('Error saving schedule:', error);
        this.submitting = false;
        this.notificationService.error('Failed to save schedule');
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/irrigation/schedules']);
  }
}
