import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { deleteCookie } from 'cookies-next';

import { AuthUser } from '@/types/auth';

interface AuthState {
    user: AuthUser | null;
}

const initialState: AuthState = {
    user: null,
};

const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
        loginSuccess(state, action: PayloadAction<AuthUser>) {
            state.user = action.payload;
        },
        logout(state) {
            deleteCookie('token');
            state.user = null;
        },
    },
});

export const { loginSuccess, logout } = authSlice.actions;
export default authSlice.reducer;
