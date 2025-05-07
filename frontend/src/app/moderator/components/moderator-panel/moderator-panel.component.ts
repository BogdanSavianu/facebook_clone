import { Component, OnInit } from '@angular/core';
import { UserService } from '../../../shared/services/user.service';
import { User } from '../../../auth/models/user.model';
import { HttpErrorResponse } from '@angular/common/http';

interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

@Component({
  selector: 'app-moderator-panel',
  templateUrl: './moderator-panel.component.html',
  styleUrls: ['./moderator-panel.component.scss']
})
export class ModeratorPanelComponent implements OnInit {
  searchQuery = '';
  users: User[] = [];
  isLoading = false;
  errorMessage = '';

  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;

  constructor(private userService: UserService) { }

  ngOnInit(): void {
  }

  onSearch(): void {
    if (!this.searchQuery.trim()) {
      this.users = [];
      this.totalPages = 0;
      this.totalElements = 0;
      return;
    }
    this.isLoading = true;
    this.userService.searchUsers(this.searchQuery, this.currentPage, this.pageSize).subscribe({
      next: (data: Page<User>) => {
        this.users = data.content;
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;
        this.isLoading = false;
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message || 'Error searching users';
        this.isLoading = false;
      }
    });
  }

  banUser(userId: number): void {
    if (!userId) return;
    this.userService.banUser(userId).subscribe({
      next: (updatedUser) => {
        this.updateUserInList(updatedUser);
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message || 'Error banning user';
        alert(`Error banning user: ${this.errorMessage}`);
      }
    });
  }

  unbanUser(userId: number): void {
    if (!userId) return;
    this.userService.unbanUser(userId).subscribe({
      next: (updatedUser) => {
        this.updateUserInList(updatedUser);
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message || 'Error unbanning user';
        alert(`Error unbanning user: ${this.errorMessage}`);
      }
    });
  }

  private updateUserInList(updatedUser: User): void {
    const index = this.users.findIndex(u => u.id === updatedUser.id);
    if (index !== -1) {
      this.users[index] = updatedUser;
      this.users = [...this.users]; 
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.onSearch();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.onSearch();
    }
  }
}
