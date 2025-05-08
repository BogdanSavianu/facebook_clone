import { ComponentFixture, TestBed, waitForAsync, fakeAsync, tick } from '@angular/core/testing';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Component } from '@angular/core';

import { RegisterComponent } from './register.component';
import { AuthService } from '../../services/auth.service';

// Create a dummy component for routing
@Component({template: ''})
class DummyLoginComponent {}

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let mockAuthService: any;
  let router: Router;

  beforeEach(waitForAsync(() => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['register']);

    TestBed.configureTestingModule({
      declarations: [RegisterComponent, DummyLoginComponent],
      imports: [
        ReactiveFormsModule,
        RouterTestingModule.withRoutes([
          { path: 'login', component: DummyLoginComponent }
        ]),
        HttpClientTestingModule 
      ],
      providers: [
        FormBuilder, 
        { provide: AuthService, useValue: mockAuthService }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.callThrough();
    mockAuthService.register.and.returnValue(of({ message: 'User registered successfully!' }));
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  describe('Form Initialization and Validation', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should initialize registerForm with username, email, and password controls', () => {
      expect(component.registerForm).toBeDefined();
      expect(component.registerForm.get('username')).toBeTruthy();
      expect(component.registerForm.get('email')).toBeTruthy();
      expect(component.registerForm.get('password')).toBeTruthy();
    });

    it('should make the form invalid if required fields are empty', () => {
      component.registerForm.setValue({ username: '', email: '', password: '' });
      expect(component.registerForm.invalid).toBeTrue();
      expect(component.registerForm.get('username')?.hasError('required')).toBeTrue();
      expect(component.registerForm.get('email')?.hasError('required')).toBeTrue();
      expect(component.registerForm.get('password')?.hasError('required')).toBeTrue();
    });

    it('should make the form invalid for invalid email format', () => {
      component.registerForm.patchValue({ email: 'invalid-email' });
      expect(component.registerForm.get('email')?.hasError('email')).toBeTrue();
    });
    
    // Add more tests for minLength/maxLength if needed
  });

  describe('Registration Submission', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should call authService.register, set success flag, and navigate on successful submission', fakeAsync(() => {
      const registerData = { username: 'testuser', email: 'test@example.com', password: 'password123' };
      component.registerForm.setValue(registerData);
      expect(component.registerForm.valid).toBeTrue();

      component.onSubmit();

      expect(mockAuthService.register).toHaveBeenCalledOnceWith(registerData);
      expect(component.isSuccessful).toBeTrue();
      expect(component.isSignUpFailed).toBeFalse();
      
      tick(2000); 
      
      expect(router.navigate).toHaveBeenCalledOnceWith(['/login']);
    }));

    it('should not call authService.register if form is invalid', () => {
      component.registerForm.setValue({ username: '', email: '', password: '' });
      expect(component.registerForm.invalid).toBeTrue();

      component.onSubmit();

      expect(mockAuthService.register).not.toHaveBeenCalled();
    });

  });

}); 