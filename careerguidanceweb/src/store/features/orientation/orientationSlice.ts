import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { HollandCategory } from '@/components/orientation/HollandQuestions';

/* ── Shared types (re-exported for consumers) ── */
export interface SubjectAvg {
    name: string;
    score: number;
    icon: string;
}

export interface SurveyData {
    has_part_time_job: boolean;
    extracurricular_activities: boolean;
    self_study_hours: number;
    gender: string;
    absences: number;
}

export type OrientationStep = 'profile' | 'holland' | 'results';

interface OrientationState {
    step: OrientationStep;
    isLoading: boolean;
    savedScores: SubjectAvg[];
    savedSurvey: SurveyData | null;
    academicResult: string | null;
    hollandResult: string | null;
    sessionId: string | null;
    isHollandCompleted: boolean;
    hollandScores: Record<HollandCategory, number> | null;
}

const initialState: OrientationState = {
    step: 'profile',
    isLoading: false,
    savedScores: [],
    savedSurvey: null,
    academicResult: null,
    hollandResult: null,
    sessionId: null,
    isHollandCompleted: false,
    hollandScores: null,
};

const orientationSlice = createSlice({
    name: 'orientation',
    initialState,
    reducers: {
        setStep(state, action: PayloadAction<OrientationStep>) {
            state.step = action.payload;
        },
        setLoading(state, action: PayloadAction<boolean>) {
            state.isLoading = action.payload;
        },
        setSavedData(state, action: PayloadAction<{ scores: SubjectAvg[]; survey: SurveyData }>) {
            state.savedScores = action.payload.scores;
            state.savedSurvey = action.payload.survey;
        },
        setAcademicResult(state, action: PayloadAction<string | null>) {
            state.academicResult = action.payload;
        },
        setHollandResult(state, action: PayloadAction<string | null>) {
            state.hollandResult = action.payload;
        },
        setSessionId(state, action: PayloadAction<string | null>) {
            state.sessionId = action.payload;
        },
        setHollandCompleted(state, action: PayloadAction<{ scores: Record<HollandCategory, number> }>) {
            state.isHollandCompleted = true;
            state.hollandScores = action.payload.scores;
        },
        resetOrientation() {
            return initialState;
        },
    },
});

export const {
    setStep,
    setLoading,
    setSavedData,
    setAcademicResult,
    setHollandResult,
    setSessionId,
    setHollandCompleted,
    resetOrientation,
} = orientationSlice.actions;

export default orientationSlice.reducer;
