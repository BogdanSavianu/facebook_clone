export interface Post {
  id: number;
  authorId: number;
  authorUsername: string;
  authorUserScore?: number;
  title: string;
  content: string;
  createdAt: string;
  imageUrl?: string;
  status: PostStatus;
  voteCount: number;
  tags: Tag[];
  commentCount?: number;
}

export enum PostStatus {
  JUST_POSTED = 'JUST_POSTED',
  FIRST_REACTIONS = 'FIRST_REACTIONS',
  OUTDATED = 'OUTDATED'
}

export interface Tag {
  id: number;
  name: string;
}

export interface PostRequest {
  title: string;
  content: string;
  imageUrl?: string;
  tagNames?: string[];
} 