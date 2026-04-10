'use client';

import moment from "moment";
import { useTranslation } from "react-i18next";
import { useEffect } from "react";
// @ts-ignore
import "moment/locale/vi";

interface TimeConvertProps {
    timestamp: string | number | Date;
}

export default function TimeConvert({ timestamp }: TimeConvertProps) {
    const { i18n } = useTranslation();

    useEffect(() => {
        moment.locale(i18n.language);
    }, [i18n.language]);

    if (!timestamp) return null;

    const time = typeof timestamp === "number"
        ? moment(timestamp)
        : moment(timestamp);

    return <span>{time.fromNow()}</span>;
}
