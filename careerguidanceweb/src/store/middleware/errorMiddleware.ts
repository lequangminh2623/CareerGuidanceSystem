import { isRejectedWithValue } from '@reduxjs/toolkit';
import type { MiddlewareAPI, Middleware } from '@reduxjs/toolkit';
import { showNotification } from '@/store/features/ui/uiSlice';
import { getErrorMessage } from '@/store/features/api/apiSlice';
import i18n from '@/lib/i18n';

export const rtkQueryErrorLogger: Middleware =
    (api: MiddlewareAPI) => (next) => (action) => {
        // RTK Query uses isRejectedWithValue when endpoints fail
        if (isRejectedWithValue(action)) {
            const payload = action.payload as any;
            const status = payload?.status;

            if (status === 401) {
                // Determine whether to dispatch notification based on URL
                // If already on login page, we might not want to spam, but let's show it anyway
                api.dispatch(
                    showNotification({
                        type: 'warning',
                        title: i18n.t('session-expired-title'),
                        message: i18n.t('session-expired-desc'),
                    })
                );

                // Short delay to allow the notification to be seen/stored before redirect
                setTimeout(() => {
                    if (typeof window !== 'undefined') {
                        window.location.href = '/login';
                    }
                }, 100);
            } else if (status === 403) {
                api.dispatch(
                    showNotification({
                        type: 'warning',
                        title: i18n.t('forbidden-toast-title'),
                        message: i18n.t('forbidden-toast-desc'),
                    })
                );

                setTimeout(() => {
                    if (typeof window !== 'undefined') {
                        window.location.href = '/forbidden'; // Giả định route 403 là forbidden
                    }
                }, 100);
            } else if (status >= 500) {
                const message = getErrorMessage(payload);
                api.dispatch(
                    showNotification({
                        type: 'error',
                        title: i18n.t('error'),
                        message: message || i18n.t('system-error') || 'Đã có lỗi xảy ra từ máy chủ, vui lòng thử lại sau.',
                    })
                );
            } else {
                // Let other errors handle themselves in components unless we want to catch ALL here
                // For now, components handle 400s themselves, we mainly want to catch 401s and 500s globally
            }
        }

        return next(action);
    };
