import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { WeatherService } from '../../services/weather.service';
import { IrrigationService } from '../../services/irrigation.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatGridListModule,
    MatIconModule,
    MatProgressBarModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  stationCount = 0;
  forecastCount = 0;
  scheduleCount = 0;
  logCount = 0;
  loading = true;

  constructor(
    private weatherService: WeatherService,
    private irrigationService: IrrigationService
  ) { }

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading = true;

    this.weatherService.getAllStations().subscribe({
      next: (stations) => {
        this.stationCount = stations.length;
      },
      error: (error) => {
        console.error('Error loading stations:', error);
      }
    });

    this.weatherService.getPrevisions(1).subscribe({
      next: (forecasts) => {
        this.forecastCount = forecasts.length;
      },
      error: (error) => {
        console.error('Error loading forecasts:', error);
      }
    });

    this.irrigationService.getProgrammes().subscribe({
      next: (schedules) => {
        this.scheduleCount = schedules.length;
      },
      error: (error) => {
        console.error('Error loading schedules:', error);
      }
    });

    this.irrigationService.getJournalEntries().subscribe({
      next: (logs) => {
        this.logCount = logs.length;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading logs:', error);
        this.loading = false;
      }
    });
  }
}
