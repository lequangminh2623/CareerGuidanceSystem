import { configureStore } from '@reduxjs/toolkit';
import authReducer from '@/store/features/auth/authSlice';
import orientationReducer from '@/store/features/orientation/orientationSlice';
import uiReducer from '@/store/features/ui/uiSlice';
import { apiSlice } from '@/store/features/api/apiSlice';
import { rtkQueryErrorLogger } from '@/store/middleware/errorMiddleware';

export const store = configureStore({
    reducer: {
        auth: authReducer,
        orientation: orientationReducer,
        ui: uiReducer,
        [apiSlice.reducerPath]: apiSlice.reducer,
    },
    middleware: (getDefaultMiddleware) =>
        getDefaultMiddleware().concat(apiSlice.middleware, rtkQueryErrorLogger),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
