import { deleteCookie } from "cookies-next";
import { User, Action } from "@/lib/contexts/userContext";

export default function myUserReducer(state: User | null, action: Action): User | null {
    switch (action.type) {
        case "login":
            return action.payload;
        case "logout":
            deleteCookie('token');
            return null;
        default:
            return state;
    }
}