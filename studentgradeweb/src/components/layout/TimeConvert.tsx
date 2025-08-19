'use client';

import moment from "moment";
import "moment/locale/vi";

interface TimeConvertProps {
    timestamp: string | number | Date;
}

moment.locale("vi");

export default function TimeConvert({ timestamp }: TimeConvertProps) {
    if (!timestamp) return null;

    const time = typeof timestamp === "number"
        ? moment(timestamp)
        : moment(timestamp);

    return <span>{time.fromNow()}</span>;
}
