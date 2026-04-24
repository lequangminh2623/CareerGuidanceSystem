import { useEffect, useRef, useState } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getCookie } from "cookies-next";
import { BASE_URL } from "@/lib/utils/api";

type EventType = "DEVICE_DISCOVERED" | "ATTENDANCE_RECORDED";

export interface WebSocketEvent<T = any> {
    eventType: EventType;
    data: T;
    timestamp: string;
}

export const useWebSocket = () => {
    const stompClientRef = useRef<Client | null>(null);
    const [latestDeviceEvent, setLatestDeviceEvent] = useState<WebSocketEvent | null>(null);
    const [latestAttendanceEvent, setLatestAttendanceEvent] = useState<WebSocketEvent | null>(null);
    const [isConnected, setIsConnected] = useState(false);

    useEffect(() => {
        const token = getCookie("token")?.toString();

        // Admin-service direct to backend, but Frontend going through API Gateway
        // We configure API Gateway to route /attendance-service/ws/** to attendance-service STOMP endpoint
        const wsUrl = `${BASE_URL}/attendance-service/ws`;

        const client = new Client({
            // Note: SockJS fallback logic
            webSocketFactory: () => new SockJS(wsUrl),
            // Pass JWT token in connectHeaders
            connectHeaders: token ? {
                Authorization: `Bearer ${token}`
            } : {},
            debug: (str) => {
                // console.log('[STOMP] ' + str);
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        client.onConnect = (frame) => {
            console.log("WebSocket Connected:", frame);
            setIsConnected(true);

            // Subscribe to device discoveries
            client.subscribe("/topic/devices", (message) => {
                if (message.body) {
                    const event: WebSocketEvent = JSON.parse(message.body);
                    setLatestDeviceEvent(event);
                }
            });

            // Subscribe to attendance records
            client.subscribe("/topic/attendances", (message) => {
                if (message.body) {
                    const event: WebSocketEvent = JSON.parse(message.body);
                    setLatestAttendanceEvent(event);
                }
            });
        };

        client.onStompError = (frame) => {
            console.error("Broker reported error: " + frame.headers["message"]);
            console.error("Additional details: " + frame.body);
        };

        client.onWebSocketClose = () => {
            console.log("WebSocket Disconnected");
            setIsConnected(false);
        };

        client.activate();
        stompClientRef.current = client;

        return () => {
            client.deactivate();
        };
    }, []);

    return {
        isConnected,
        latestDeviceEvent,
        latestAttendanceEvent,
    };
};
