'use client';

import { useContext } from "react";
import { MyUserContext } from "@/lib/contexts/userContext";
import StatisticsClient from "@/components/statistics";

const HomeClient = () => {
    const user = useContext(MyUserContext);
    const role = user?.role;

    // If student or teacher, render statistics as homepage
    if (role === "Student" || role === "Teacher") {
        return <StatisticsClient />;
    }

    return null;
};

export default HomeClient;
