import { useDispatch, useSelector } from 'react-redux';
import type { RootState, AppDispatch } from '@/store/store';

/** Typed dispatch hook — use this instead of `useDispatch` */
export const useAppDispatch = useDispatch.withTypes<AppDispatch>();

/** Typed selector hook — use this instead of `useSelector` */
export const useAppSelector = useSelector.withTypes<RootState>();
