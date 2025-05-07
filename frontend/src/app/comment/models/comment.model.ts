export interface Comment {
  id: number;
  postId: number;
  authorId: number;
  authorUsername: string;
  authorScore: number;
  content: string;
  createdAt: string;
  imageUrl?: string;
  voteCount: number;
  parentId?: number;
  replies?: Comment[];
  isReplyFormVisible?: boolean;
  isLoadingReplies?: boolean;
  repliesLoaded?: boolean;
  replyCount?: number;
}

export interface CommentRequest {
  postId: number;
  content: string;
  imageUrl?: string;
  parentId?: number;
} 