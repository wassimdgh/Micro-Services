import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { IrrigationService, ProgrammeArrosage } from '../../../services/irrigation.service';
import { NotificationService } from '../../../services/notification.service';

@Component({
  selector: 'app-schedule-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './schedule-list.component.html',
  styleUrls: ['./schedule-list.component.scss']
})
export class ScheduleListComponent implements OnInit {
  schedules: ProgrammeArrosage[] = [];
  loading = true;
  displayedColumns: string[] = ['id', 'parcelleId', 'datePlanifiee', 'duree', 'volumePrevu', 'statut', 'adjustment', 'actions'];

  constructor(
    private irrigationService: IrrigationService,
    private notificationService: NotificationService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadSchedules();
  }

  loadSchedules(): void {
    this.loading = true;
    this.irrigationService.getProgrammes().subscribe({
      next: (data) => {
        this.schedules = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading schedules:', error);
        this.notificationService.error('Failed to load schedules');
        this.loading = false;
      }
    });
  }

  createNew(): void {
    this.router.navigate(['/irrigation/schedules/new']);
  }

  edit(schedule: ProgrammeArrosage): void {
    this.router.navigate(['/irrigation/schedules', schedule.id, 'edit']);
  }

  delete(schedule: ProgrammeArrosage): void {
    console.log('Delete clicked for schedule ID:', schedule.id);
    if (confirm(`Are you sure you want to delete this schedule?`)) {
      console.log('Confirmed deletion, calling API...');
      this.irrigationService.deleteProgramme(schedule.id!).subscribe({
        next: () => {
          console.log('Schedule deleted successfully');
          this.notificationService.success('Schedule deleted successfully');
          this.loadSchedules();
        },
        error: (error) => {
          console.error('Error deleting schedule:', error);
          this.notificationService.error('Failed to delete schedule: ' + (error?.status || 'Unknown error'));
        }
      });
    }
  }

  getStatusColor(statut: string): string {
    switch (statut) {
      case 'PENDING': return 'primary';
      case 'PLANIFIE': return 'accent';
      case 'EXECUTED': return 'success';
      case 'REPLANIFIE': return 'warn';
      case 'FAILED': return 'warn';
      case 'CANCELLED': return 'disabled';
      default: return 'primary';
    }
  }

  getStatusBackgroundClass(statut: string): string {
    switch (statut) {
      case 'PENDING': return 'status-pending';
      case 'PLANIFIE': return 'status-planned';
      case 'EXECUTED': return 'status-executed';
      case 'REPLANIFIE': return 'status-replanifie';
      case 'FAILED': return 'status-failed';
      case 'CANCELLED': return 'status-cancelled';
      default: return 'status-default';
    }
  }

  getAdjustmentInfo(schedule: ProgrammeArrosage): string {
    if (schedule.statut === 'REPLANIFIE') {
      return '⏭️ Postponed (Weather)';
    }
    // In future, check if volumePrevu was adjusted
    return '-';
  }
}
