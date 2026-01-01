import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { WeatherService, StationMeteo } from '../../../services/weather.service';
import { NotificationService } from '../../../services/notification.service';

@Component({
  selector: 'app-forecast-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule
  ],
  templateUrl: './forecast-form.component.html',
  styleUrls: ['./forecast-form.component.scss']
})
export class ForecastFormComponent implements OnInit {
  form!: FormGroup;
  submitting = false;
  stations: StationMeteo[] = [];

  constructor(
    private fb: FormBuilder,
    private weatherService: WeatherService,
    private notificationService: NotificationService,
    private router: Router
  ) {
    this.initializeForm();
  }

  ngOnInit(): void {
    this.loadStations();
  }

  initializeForm(): void {
    this.form = this.fb.group({
      stationId: ['', Validators.required],
      date: ['', Validators.required],
      temperatureMax: ['', [Validators.required, Validators.pattern(/^-?[0-9]+(\.[0-9]+)?$/)]],
      temperatureMin: ['', [Validators.required, Validators.pattern(/^-?[0-9]+(\.[0-9]+)?$/)]],
      pluiePrevue: ['', [Validators.required, Validators.pattern(/^[0-9]+(\.[0-9]+)?$/)]],
      vent: ['', [Validators.required, Validators.pattern(/^[0-9]+(\.[0-9]+)?$/)]]
    });
  }

  loadStations(): void {
    this.weatherService.getAllStations().subscribe({
      next: (stations) => {
        this.stations = stations;
        if (stations.length === 1) {
          this.form.patchValue({ stationId: stations[0].id });
        }
      },
      error: (error) => {
        console.error('Error loading stations:', error);
        this.notificationService.error('No weather stations available. Please create one first.');
        this.router.navigate(['/weather/stations']);
      }
    });
  }

  submit(): void {
    if (!this.form.valid) {
      this.notificationService.warning('Please fill in all required fields correctly');
      return;
    }

    this.submitting = true;
    const formData = this.form.value;

    this.weatherService.createPrevision(formData).subscribe({
      next: () => {
        this.submitting = false;
        this.notificationService.success('Forecast created successfully');
        this.router.navigate(['/weather/forecasts']);
      },
      error: (error) => {
        console.error('Error creating forecast:', error);
        this.submitting = false;
        this.notificationService.error('Failed to create forecast');
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/weather/forecasts']);
  }
}
