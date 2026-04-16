import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export type NotificationType = 'success' | 'error' | 'warning' | 'info';
export type DisplayMode = 'modal' | 'toast';

interface NotificationState {
    isOpen: boolean;
    type: NotificationType;
    displayMode: DisplayMode;
    title: string;
    message: string;
    confirmText?: string;
}

const initialState: NotificationState = {
    isOpen: false,
    type: 'info',
    displayMode: 'toast',
    title: '',
    message: '',
    confirmText: 'OK',
};

const uiSlice = createSlice({
    name: 'ui',
    initialState,
    reducers: {
        showNotification: (state, action: PayloadAction<Omit<NotificationState, 'isOpen' | 'displayMode'> & { displayMode?: DisplayMode }>) => {
            state.isOpen = true;
            state.type = action.payload.type;
            state.displayMode = action.payload.displayMode || 'toast';
            state.title = action.payload.title;
            state.message = action.payload.message;
            state.confirmText = action.payload.confirmText || 'OK';
        },
        hideNotification: (state) => {
            state.isOpen = false;
        },
    },
});

export const { showNotification, hideNotification } = uiSlice.actions;
export default uiSlice.reducer;
