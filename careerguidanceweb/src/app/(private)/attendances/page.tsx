import React from "react";
import AttendancesClient from "@/components/attendances/AttendancesClient";

export const metadata = {
    title: "Scholar - Attendances",
    description: "Search and view attendance history",
};

export default function AttendancesPage() {
    return <AttendancesClient />;
}
