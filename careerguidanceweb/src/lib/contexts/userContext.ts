import { createContext, Dispatch } from "react";

export interface User {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
    avatar: string;
    createdDate?: string;
    updatedDate?: string;
    active: boolean;
    role: string;

    // Quan hệ
    student?: Student;
}

// ví dụ thêm type liên quan
export interface Student {
    id: number;
    code: string;
}

// Action type cho reducer
export type Action =
    | { type: "login"; payload: User }
    | { type: "logout" };

// Context lưu user hiện tại
export const MyUserContext = createContext<User | null>(null);

// Context để dispatch action
export const MyDispatcherContext = createContext<Dispatch<Action> | null>(null);
