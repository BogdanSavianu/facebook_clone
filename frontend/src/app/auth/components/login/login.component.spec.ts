import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ActivatedRoute } from '@angular/router';

import { LoginComponent } from './login.component';
import { AuthService } from '../../services/auth.service';
import { TokenStorageService } from '../../services/token-storage.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let mockAuthService: any;
  let mockTokenStorageService: any;
  let router: Router;
  let fb: FormBuilder;

  beforeEach(waitForAsync(() => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['login']);
    mockTokenStorageService = jasmine.createSpyObj('TokenStorageService', ['saveToken', 'saveUser', 'getUser', 'getToken']);

    TestBed.configureTestingModule({
      declarations: [LoginComponent],
      imports: [
        ReactiveFormsModule,
        RouterTestingModule.withRoutes([]),
        HttpClientTestingModule 
      ],
      providers: [
        FormBuilder, 
        { provide: AuthService, useValue: mockAuthService },
        { provide: TokenStorageService, useValue: mockTokenStorageService },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { 
              queryParams: {}
            }
          }
        }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fb = TestBed.inject(FormBuilder);
    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.callThrough();

    mockAuthService.login.and.returnValue(of({ token: 'test-token', id: 1, username: 'testuser', email: 'test@example.com', roles: ['ROLE_USER'] }));
    mockTokenStorageService.getUser.and.returnValue(null); 
    mockTokenStorageService.getToken.and.returnValue(null);
  });

  it('should create', () => {
    fixture.detectChanges(); 
    expect(component).toBeTruthy();
  });

  describe('Form Initialization and Validation', () => {
    it('should initialize loginForm with username and password controls on ngOnInit', () => {
      fixture.detectChanges(); 
      expect(component.loginForm).toBeDefined();
      expect(component.loginForm.get('username')).toBeTruthy();
      expect(component.loginForm.get('password')).toBeTruthy();
    });

    it('should make the form invalid if username is not provided', () => {
      fixture.detectChanges();
      component.loginForm.setValue({ username: '', password: 'password123' });
      expect(component.loginForm.invalid).toBeTrue();
      expect(component.loginForm.get('username')?.hasError('required')).toBeTrue(); 
    });

    it('should make the form invalid if password is not provided', () => {
      fixture.detectChanges();
      component.loginForm.setValue({ username: 'testuser', password: '' });
      expect(component.loginForm.invalid).toBeTrue();
      expect(component.loginForm.get('password')?.hasError('required')).toBeTrue(); 
    });
  });

  describe('Login Submission', () => {
    beforeEach(() => {
      fixture.detectChanges(); 
    });

    it('should call authService.login and navigate on successful submission', () => {
      const loginData = { username: 'testuser', password: 'password123' };
      const expectedUserData = { token: 'test-token', id: 1, username: 'testuser', email: 'test@example.com', roles: ['ROLE_USER'] };
      component.loginForm.setValue(loginData);
      expect(component.loginForm.valid).toBeTrue();

      component.onSubmit();

      expect(mockAuthService.login).toHaveBeenCalledOnceWith(loginData);
      expect(mockTokenStorageService.saveToken).toHaveBeenCalledOnceWith('test-token');
      expect(mockTokenStorageService.saveUser).toHaveBeenCalledOnceWith(expectedUserData);
      expect(router.navigate).toHaveBeenCalledOnceWith(['/']);
      expect(component.isLoginFailed).toBeFalse();
      expect(component.isLoggedIn).toBeTrue();
    });

    it('should not call authService.login if form is invalid', () => {
      component.loginForm.setValue({ username: '', password: '' }); 
      expect(component.loginForm.invalid).toBeTrue();

      component.onSubmit();

      expect(mockAuthService.login).not.toHaveBeenCalled();
    });
  });
}); 