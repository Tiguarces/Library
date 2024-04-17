import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Book } from '../model/Book';
import { BookResponse } from '../model/BookResponse';
import { CreateBookRequest } from '../model/CreateBookRequest';
import { SearchBookRequest } from '../model/SearchBookRequest';
import { BookDetailsResponse } from '../model/BookDetailsResponse';

@Injectable({
  providedIn: 'root'
})
export class BookService {
  private readonly BASE_API_URL = 'http://localhost:8080/api/book';

  constructor(private httpClient: HttpClient) { }

  fetchBooks(searchReqeuest: SearchBookRequest): Observable<BookResponse> {
    return this.httpClient.post<BookResponse>(`${this.BASE_API_URL}/fetch/all`, searchReqeuest);
  }

  fetchById(id: number): Observable<BookDetailsResponse> {
    return this.httpClient.get<BookDetailsResponse>(`${this.BASE_API_URL}/fetch/` + id);
  }

  create(book: CreateBookRequest): Observable<any> {
    return this.httpClient.post(`${this.BASE_API_URL}/create`, book);
  }
}
