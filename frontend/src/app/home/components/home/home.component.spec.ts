import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { HomeComponent } from './home.component';
import { PostService, PostFilters } from '../../../post/services/post.service';
import { ReactionService } from '../../../reaction/services/reaction.service';
import { TokenStorageService } from '../../../auth/services/token-storage.service';
import { UserService } from '../../../shared/services/user.service';
import { Post, Tag } from '../../../post/models/post.model';
import { User } from '../../../auth/models/user.model';


describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;
  let mockPostService: any;
  let mockReactionService: any;
  let mockTokenStorageService: any;
  let mockUserService: any;

  const mockPostsPage = { 
    content: [{ id: 1, title: 'Post 1', authorId: 1 }, { id: 2, title: 'Post 2', authorId: 2 }] as Post[],
    totalPages: 1,
    totalElements: 2,
    size: 10,
    number: 0
  };
  const mockUser: User = { id: 1, username: 'testuser', email: 'test@example.com', roles: ['ROLE_USER'], userScore: 0, banned: false };
  const mockTags: Tag[] = [{ id: 1, name: 'angular' }, { id: 2, name: 'testing' }];

  beforeEach(waitForAsync(() => {
    mockPostService = jasmine.createSpyObj('PostService', ['getAllPosts', 'getAllTags']);
    mockReactionService = jasmine.createSpyObj('ReactionService', ['getReactionStats']);
    mockTokenStorageService = jasmine.createSpyObj('TokenStorageService', ['getUser', 'saveUser']); 
    mockUserService = jasmine.createSpyObj('UserService', ['searchUsers', 'promoteSelfToModerator']);

    mockPostService.getAllPosts.and.returnValue(of(mockPostsPage));
    mockPostService.getAllTags.and.returnValue(of(mockTags)); 
    mockReactionService.getReactionStats.and.returnValue(of({ userReaction: null, totalScore: 0 }));
    mockTokenStorageService.getUser.and.returnValue(mockUser);


    TestBed.configureTestingModule({
      declarations: [HomeComponent],
      imports: [ 
        HttpClientTestingModule, 
        RouterTestingModule
      ],
      providers: [
        { provide: PostService, useValue: mockPostService },
        { provide: ReactionService, useValue: mockReactionService },
        { provide: TokenStorageService, useValue: mockTokenStorageService },
        { provide: UserService, useValue: mockUserService }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    spyOn(component, 'loadPosts').and.callThrough(); 
  });

  it('should create', () => {
    fixture.detectChanges(); 
    expect(component).toBeTruthy();
  });

  it('should call loadPosts and loadAllTags on init', () => {
    spyOn(component, 'loadAllTags').and.callThrough();
    fixture.detectChanges(); 
    expect(component.loadPosts).toHaveBeenCalledWith(); 
    expect(component.loadAllTags).toHaveBeenCalled();
    expect(mockPostService.getAllPosts).toHaveBeenCalled();
    expect(mockPostService.getAllTags).toHaveBeenCalled();
    expect(component.allTags).toEqual(mockTags);
  });

  describe('Filtering Posts', () => {
    beforeEach(() => {
      fixture.detectChanges(); 
      (component.loadPosts as jasmine.Spy).calls.reset();
      mockPostService.getAllPosts.calls.reset();
    });

    it('should apply tag filter and reload posts', () => {
      const tagName = 'testing';
      component.applyTagFilter(tagName);

      expect(component.filterTagName).toBe(tagName);
      expect(component.loadPosts).toHaveBeenCalledWith(true); 
      
      const expectedFilters: PostFilters = { 
        tagName: tagName, 
        titleSearch: undefined, 
        authorId: undefined, 
        myPosts: undefined 
      };
      expect(mockPostService.getAllPosts).toHaveBeenCalledWith(0, 10, expectedFilters);
    });

    it('should apply title filter and reload posts', () => {
      const title = 'search term';
      component.applyTitleSearch(title);

      expect(component.filterTitleSearch).toBe(title);
      expect(component.loadPosts).toHaveBeenCalledWith(true);
      
      const expectedFilters: PostFilters = { 
        tagName: undefined, 
        titleSearch: title, 
        authorId: undefined, 
        myPosts: undefined 
      };
      expect(mockPostService.getAllPosts).toHaveBeenCalledWith(0, 10, expectedFilters);
    });

    it('should apply author filter and reload posts', () => {
      const authorId = 123;
      component.filterByAuthor(authorId);

      expect(component.filterAuthorId).toBe(authorId);
      expect(component.loadPosts).toHaveBeenCalledWith(true);
      
      const expectedFilters: PostFilters = { 
        tagName: undefined, 
        titleSearch: undefined, 
        authorId: authorId, 
        myPosts: undefined 
      };
      expect(mockPostService.getAllPosts).toHaveBeenCalledWith(0, 10, expectedFilters);
    });

    it('should apply myPosts filter and reload posts', () => {
      component.showMyPosts();

      expect(component.filterMyPosts).toBe(true);
      expect(component.loadPosts).toHaveBeenCalledWith(true);
      
      const expectedFilters: PostFilters = { 
        tagName: undefined, 
        titleSearch: undefined, 
        authorId: undefined, 
        myPosts: true 
      };
      expect(mockPostService.getAllPosts).toHaveBeenCalledWith(0, 10, expectedFilters);
    });

    it('should clear all filters and reload posts when showAllPosts is called', () => {
      component.filterTagName = 'angular';
      component.filterAuthorId = 1;
      component.filterMyPosts = true;
      component.filterTitleSearch = 'a title';

      component.showAllPosts();

      expect(component.filterTagName).toBeUndefined();
      expect(component.filterTitleSearch).toBeUndefined();
      expect(component.filterAuthorId).toBeUndefined();
      expect(component.filterMyPosts).toBeUndefined();
      expect(component.loadPosts).toHaveBeenCalledWith(true);
      
      const expectedFilters: PostFilters = { 
        tagName: undefined, 
        titleSearch: undefined, 
        authorId: undefined, 
        myPosts: undefined 
      };
      expect(mockPostService.getAllPosts).toHaveBeenCalledWith(0, 10, expectedFilters);
    });

  });

}); 