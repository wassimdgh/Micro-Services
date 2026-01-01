import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { IrrigationService, JournalArrosage } from '../../../services/irrigation.service';
import { NotificationService } from '../../../services/notification.service';

@Component({
  selector: 'app-execution-log-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './execution-log-list.component.html',
  styleUrls: ['./execution-log-list.component.scss']
})
export class ExecutionLogListComponent implements OnInit {
  logs: JournalArrosage[] = [];
  loading = true;
  displayedColumns: string[] = ['id', 'programmeId', 'dateExecution', 'volumeReel', 'remarque', 'actions'];

  constructor(
    private irrigationService: IrrigationService,
    private notificationService: NotificationService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadLogs();
  }

  loadLogs(): void {
    this.loading = true;
    this.irrigationService.getJournalEntries().subscribe({
      next: (data) => {
        this.logs = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading logs:', error);
        this.notificationService.error('Failed to load execution logs');
        this.loading = false;
      }
    });
  }

  createNew(): void {
    this.router.navigate(['/irrigation/logs/new']);
  }

  delete(log: JournalArrosage): void {
    if (confirm(`Are you sure you want to delete this log?`)) {
      this.irrigationService.deleteJournalEntry(log.id!).subscribe({
        next: () => {
          this.notificationService.success('Log deleted successfully');
          this.loadLogs();
        },
        error: (error) => {
          console.error('Error deleting log:', error);
          this.notificationService.error('Failed to delete log: ' + (error?.status || 'Unknown error'));
        }
      });
    }
  }
}
