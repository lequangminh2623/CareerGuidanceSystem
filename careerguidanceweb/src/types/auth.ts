export enum UserRole {
    ADMIN = 'Admin',
    TEACHER = 'Teacher',
    STUDENT = 'Student',
}

export interface Student {
    id: number;
    code: string;
}

export interface AuthUser {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
    avatar: string;
    createdDate?: string;
    updatedDate?: string;
    active: boolean;
    role: UserRole | string;
    student?: Student;
}
