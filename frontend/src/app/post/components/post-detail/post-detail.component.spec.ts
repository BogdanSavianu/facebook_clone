import { ComponentFixture, TestBed, waitForAsync, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Component } from '@angular/core';

import { PostDetailComponent } from './post-detail.component';
import { PostService } from '../../services/post.service';
import { CommentService } from '../../../comment/services/comment.service';
import { ReactionService } from '../../../reaction/services/reaction.service';
import { TokenStorageService } from '../../../auth/services/token-storage.service';
import { Post, PostStatus } from '../../models/post.model';
import { Comment as PostComment, CommentRequest } from '../../../comment/models/comment.model';

describe('PostDetailComponent', () => {
  let component: PostDetailComponent;
  let fixture: ComponentFixture<PostDetailComponent>;
  let mockPostService: any;
  let mockCommentService: any;
  let mockReactionService: any;
  let mockTokenStorageService: any;
  let mockActivatedRoute: any;
  let router: Router;

  const mockPostData: Post = {
    id: 1,
    title: 'Test Post',
    content: 'Test Content',
    authorId: 1,
    authorUsername: 'testuser',
    authorUserScore: 10,
    createdAt: new Date().toISOString(),
    voteCount: 0,
    commentCount: 0,
    status: PostStatus.JUST_POSTED,
    tags: []
  };

  const mockCommentData: PostComment = {
    id: 100,
    postId: 1,
    authorId: 2,
    authorUsername: 'commenter',
    authorScore: 5,
    content: 'Root comment content',
    createdAt: new Date().toISOString(),
    voteCount: 2,
    replyCount: 1,
    replies: [],
    repliesLoaded: false,
    isReplyFormVisible: false,
    isLoadingReplies: false
  };

  const mockNewComment: PostComment = {
    id: 101,
    postId: 1,
    authorId: 1,
    authorUsername: 'testuser',
    content: 'New test comment',
    createdAt: new Date().toISOString(),
    voteCount: 0,
    replyCount: 0,
    replies: [],
    repliesLoaded: false,
    isReplyFormVisible: false,
    isLoadingReplies: false,
    authorScore: 0,
    imageUrl: undefined,
    parentId: undefined
  };
  
  const mockNewReply: PostComment = {
    id: 102,
    postId: 1,
    authorId: 1,
    authorUsername: 'testuser',
    content: 'New test reply',
    createdAt: new Date().toISOString(),
    voteCount: 0,
    replyCount: 0,
    replies: [],
    repliesLoaded: false,
    isReplyFormVisible: false,
    isLoadingReplies: false,
    authorScore: 0,
    imageUrl: undefined,
    parentId: 100 
  };

  beforeEach(waitForAsync(() => {
    mockPostService = jasmine.createSpyObj('PostService', ['getPostById', 'deletePost', 'updatePostStatus']);
    mockCommentService = jasmine.createSpyObj('CommentService', ['getCommentsByPostId', 'createComment', 'getReplies', 'deleteComment']);
    mockReactionService = jasmine.createSpyObj('ReactionService', ['getReactionStats', 'toggleLike', 'toggleDislike']);
    mockTokenStorageService = jasmine.createSpyObj('TokenStorageService', ['getUser', 'getToken']);

    mockActivatedRoute = {
      paramMap: of(convertToParamMap({ id: '1' })),
      snapshot: { queryParams: {} }
    };
    
    mockTokenStorageService.getUser.and.returnValue({ id: 1, username: 'testuser', roles: ['ROLE_USER'] });
    mockTokenStorageService.getToken.and.returnValue('fake-jwt-token');
    mockPostService.getPostById.and.returnValue(of(mockPostData));
    mockCommentService.getCommentsByPostId.and.returnValue(of([{...mockCommentData}]));
    mockCommentService.createComment.and.callFake((req: CommentRequest) => {
      return of(req.parentId ? {...mockNewReply} : {...mockNewComment});
    }); 
    mockReactionService.getReactionStats.and.returnValue(of({ userReaction: null, totalScore: 0 }));

    TestBed.configureTestingModule({
      declarations: [PostDetailComponent],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule.withRoutes([]), 
        ReactiveFormsModule
      ],
      providers: [
        FormBuilder, 
        { provide: PostService, useValue: mockPostService },
        { provide: CommentService, useValue: mockCommentService },
        { provide: ReactionService, useValue: mockReactionService },
        { provide: TokenStorageService, useValue: mockTokenStorageService },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PostDetailComponent);
    component = fixture.componentInstance;
    component.post = { ...mockPostData }; 
    router = TestBed.inject(Router); 
    spyOn(router, 'navigate').and.callThrough();
  });

  it('should create', () => {
    fixture.detectChanges(); 
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should load post and comments on init', () => {
      spyOn(component, 'loadPost').and.callThrough();
      spyOn(component, 'loadComments').and.callThrough();
      spyOn(component as any, 'fetchAndSetReactionStats'); 
      
      fixture.detectChanges(); 
  
      expect(component.loadPost).toHaveBeenCalledWith(1);
      expect(component.loadComments).toHaveBeenCalledWith(1);
      
      expect(mockPostService.getPostById).toHaveBeenCalledWith(1);
      expect(mockCommentService.getCommentsByPostId).toHaveBeenCalledWith(1);
      expect(mockReactionService.getReactionStats).toHaveBeenCalledWith(1, 'POST'); 
      expect(component['fetchAndSetReactionStats']).toHaveBeenCalledWith([mockCommentData]);
  
      expect(component.post).toEqual(mockPostData);
      expect(component.comments).toEqual([mockCommentData]); 
      expect(component.isPostAuthor).toBe(true); 
    });
  
    it('should initialize forms on init', () => {
      fixture.detectChanges();
      expect(component.commentForm).toBeDefined();
      expect(component.commentForm.get('content')).toBeTruthy();
      expect(component.replyForm).toBeDefined();
      expect(component.replyForm.get('content')).toBeTruthy();
    });
  });

  describe('submitComment', () => {
    beforeEach(() => {
      fixture.detectChanges(); 
      component.isCommentFormVisible = true; 
    });

    it('should call commentService.createComment and update UI on valid submission', () => {
      component.commentForm.setValue({ content: 'New test comment', imageUrl: '' });
      expect(component.commentForm.valid).toBeTrue();

      component.submitComment();

      expect(mockCommentService.createComment).toHaveBeenCalledOnceWith({
        postId: mockPostData.id,
        content: 'New test comment',
        imageUrl: undefined,
        parentId: undefined
      });
      
      expect(component.comments.length).toBe(2); 
      expect(component.comments[0]).toEqual(mockNewComment);
      expect(component.comments[1]).toEqual(mockCommentData); 
      expect(component.commentForm.pristine).toBeTrue(); 
      expect(component.isCommentFormVisible).toBeFalse();
    });

    it('should not call commentService.createComment if form is invalid', () => {
      component.commentForm.setValue({ content: '', imageUrl: '' }); 
      expect(component.commentForm.invalid).toBeTrue();

      component.submitComment();

      expect(mockCommentService.createComment).not.toHaveBeenCalled();
    });

    it('should not submit if image is uploading', () => {
      component.isUploadingCommentImage = true;
      component.commentForm.setValue({ content: 'Test', imageUrl: '' });
      component.submitComment();
      expect(mockCommentService.createComment).not.toHaveBeenCalled();
      expect(component.commentFileUploadError).toBe('Image is still uploading. Please wait.');
    });
  });

  describe('toggleCommentForm', () => {
    beforeEach(() => {
      fixture.detectChanges(); 
    });

    it('should toggle isCommentFormVisible and reset relevant properties', () => {
      component.isCommentFormVisible = false;
      component.selectedCommentFile = {} as File; 
      component.commentUploadProgress = 50;
      component.commentFileUploadError = 'error';
      component.commentPreviewUrl = 'url';
      component.isUploadingCommentImage = true;
      spyOn(component.commentForm, 'reset');

      component.toggleCommentForm(); 
      expect(component.isCommentFormVisible).toBeTrue();
      expect(component.commentForm.reset).toHaveBeenCalled();
      expect(component.selectedCommentFile).toBeNull();
      expect(component.commentUploadProgress).toBe(0);
      expect(component.commentFileUploadError).toBeNull();
      expect(component.commentPreviewUrl).toBeNull();
      expect(component.isUploadingCommentImage).toBeFalse();

      component.toggleCommentForm(); 
      expect(component.isCommentFormVisible).toBeFalse();
      expect(component.commentForm.reset).toHaveBeenCalledTimes(2); 
      expect(component.selectedCommentFile).toBeNull();
      expect(component.commentUploadProgress).toBe(0);
      expect(component.commentFileUploadError).toBeNull();
      expect(component.commentPreviewUrl).toBeNull();
      expect(component.isUploadingCommentImage).toBeFalse();
    });
  });

  describe('toggleReplyForm', () => {
    beforeEach(() => {
      fixture.detectChanges(); 
    });

    it('should toggle isReplyFormVisible for the specific comment and reset reply form', () => {
      if (component.comments.length === 0) {
         component.comments.push({...mockCommentData}); 
      }
      const testComment = component.comments[0]; 
      testComment.isReplyFormVisible = false;
      spyOn(component.replyForm, 'reset');
      spyOn(component as any, 'resetReplyUploadState'); 

      component.toggleReplyForm(testComment);
      fixture.detectChanges();

      expect(testComment.isReplyFormVisible).toBeTrue();
      expect(component.replyForm.reset).toHaveBeenCalled();
      expect(component['resetReplyUploadState']).toHaveBeenCalled();
      expect(component.activeReplyParentComment).toBe(testComment);

      component.toggleReplyForm(testComment);
      fixture.detectChanges();

      expect(testComment.isReplyFormVisible).toBeFalse();
      expect(component.replyForm.reset).toHaveBeenCalledTimes(1);
      expect(component['resetReplyUploadState']).toHaveBeenCalledTimes(2);
      expect(component.activeReplyParentComment).toBeNull();
    });
  });

  describe('submitReply', () => {
    let parentComment: PostComment;
    beforeEach(() => {
      fixture.detectChanges();
      parentComment = component.comments[0]; 
      component.toggleReplyForm(parentComment); 
      fixture.detectChanges(); 
    });

    it('should call commentService.createComment and update parent comment on valid reply submission', () => {
      component.replyForm.setValue({ content: 'New test reply', imageUrl: '' });
      expect(component.replyForm.valid).toBeTrue();

      component.submitReply(parentComment.id);

      expect(mockCommentService.createComment).toHaveBeenCalledOnceWith({
        postId: mockPostData.id,
        content: 'New test reply',
        imageUrl: undefined,
        parentId: parentComment.id
      });

      expect(parentComment.replies).toBeDefined();
      expect(parentComment.replies?.length).toBe(1);
      expect(parentComment.replies?.[0]).toEqual(mockNewReply);
      expect(parentComment.isReplyFormVisible).toBeFalse();
      expect(parentComment.repliesLoaded).toBeTrue(); 
      expect(component.replyForm.pristine).toBeTrue();
      expect(component.activeReplyParentComment).toBeNull();
    });

    it('should not call commentService.createComment if reply form is invalid', () => {
      component.replyForm.setValue({ content: '', imageUrl: '' });
      component.submitReply(parentComment.id);
      expect(mockCommentService.createComment).not.toHaveBeenCalled();
    });
  });

}); 