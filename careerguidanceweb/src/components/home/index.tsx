'use client';

import { useAppSelector } from "@/store/hooks";
import StatisticsClient from "@/components/statistics";

const HomeClient = () => {
    const user = useAppSelector((state) => state.auth.user);
    const role = user?.role;

    // If student or teacher, render statistics as homepage
    if (role === "Student" || role === "Teacher") {
        return <StatisticsClient />;
    }

    return null;
};

export default HomeClient;
