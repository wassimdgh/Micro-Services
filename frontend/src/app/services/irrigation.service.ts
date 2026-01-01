import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ProgrammeArrosage {
  id?: number;
  parcelleId: number;
  datePlanifiee: string;
  duree: number;
  volumePrevu: number;
  statut: string;
}

export interface JournalArrosage {
  id?: number;
  programmeId: number;
  dateExecution: string;
  volumeReel: number;
  remarque: string;
}

@Injectable({
  providedIn: 'root'
})
export class IrrigationService {
  private apiUrl = '/api/arrosage';

  constructor(private http: HttpClient) { }

  // Schedule Methods
  getProgrammes(): Observable<ProgrammeArrosage[]> {
    return this.http.get<ProgrammeArrosage[]>(`${this.apiUrl}/programmes`);
  }

  getProgramme(id: number): Observable<ProgrammeArrosage> {
    return this.http.get<ProgrammeArrosage>(`${this.apiUrl}/programmes/${id}`);
  }

  createProgramme(programme: ProgrammeArrosage): Observable<ProgrammeArrosage> {
    return this.http.post<ProgrammeArrosage>(`${this.apiUrl}/programmes`, programme);
  }

  updateProgramme(id: number, programme: ProgrammeArrosage): Observable<ProgrammeArrosage> {
    return this.http.put<ProgrammeArrosage>(`${this.apiUrl}/programmes/${id}`, programme);
  }

  deleteProgramme(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/programmes/${id}`);
  }

  // Journal Methods
  getJournalEntries(): Observable<JournalArrosage[]> {
    return this.http.get<JournalArrosage[]>(`${this.apiUrl}/journal`);
  }

  createJournalEntry(entry: JournalArrosage): Observable<JournalArrosage> {
    return this.http.post<JournalArrosage>(`${this.apiUrl}/journal`, entry);
  }

  deleteJournalEntry(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/journal/${id}`);
  }

  // Get Previsions
  getPrevisions(stationId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/previsions/${stationId}`);
  }
}
