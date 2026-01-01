import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { IrrigationService, JournalArrosage, ProgrammeArrosage } from '../../../services/irrigation.service';
import { NotificationService } from '../../../services/notification.service';

@Component({
  selector: 'app-execution-log-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule
  ],
  templateUrl: './execution-log-form.component.html',
  styleUrls: ['./execution-log-form.component.scss']
})
export class ExecutionLogFormComponent implements OnInit {
  form!: FormGroup;
  submitting = false;
  programmes: ProgrammeArrosage[] = [];

  constructor(
    private fb: FormBuilder,
    private irrigationService: IrrigationService,
    private notificationService: NotificationService,
    private router: Router
  ) {
    this.initializeForm();
  }

  ngOnInit(): void {
    this.loadProgrammes();
  }

  initializeForm(): void {
    this.form = this.fb.group({
      programmeId: ['', Validators.required],
      dateExecution: ['', Validators.required],
      volumeReel: ['', [Validators.required, Validators.pattern(/^[0-9]+(\.[0-9]+)?$/)]],
      remarque: ['']
    });
  }

  loadProgrammes(): void {
    this.irrigationService.getProgrammes().subscribe({
      next: (programmes) => {
        this.programmes = programmes;
        if (programmes.length === 1) {
          this.form.patchValue({ programmeId: programmes[0].id });
        }
      },
      error: (error) => {
        console.error('Error loading programmes:', error);
        this.notificationService.error('No irrigation schedules available. Please create one first.');
        this.router.navigate(['/irrigation/schedules']);
      }
    });
  }

  submit(): void {
    if (!this.form.valid) {
      this.notificationService.warning('Please fill in all required fields correctly');
      return;
    }

    this.submitting = true;
    const formData: JournalArrosage = this.form.value;

    this.irrigationService.createJournalEntry(formData).subscribe({
      next: () => {
        this.submitting = false;
        this.notificationService.success('Execution log created successfully');
        this.router.navigate(['/irrigation/logs']);
      },
      error: (error) => {
        console.error('Error creating log:', error);
        this.submitting = false;
        this.notificationService.error('Failed to create execution log');
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/irrigation/logs']);
  }
}
