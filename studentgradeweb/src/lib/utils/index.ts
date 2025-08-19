// Kiểm tra quyền (so sánh id của object với user hiện tại)
export const checkPermission = (objectUserId: string | number, userId: string | number): boolean => {
  return objectUserId === userId;
};

// Kiểm tra có thể chỉnh sửa không (trong vòng 30 phút)
export const checkCanEdit = (date: string | Date): boolean => {
  const createdTime = new Date(date);
  const now = new Date();
  const minutesSincePost = (now.getTime() - createdTime.getTime()) / 60000;
  return minutesSincePost <= 30;
};

// Format thời gian UTC sang giờ Việt Nam
export const formatVietnamTime = (utcDateStr: string): string => {
  const utcDate = new Date(utcDateStr);
  return new Intl.DateTimeFormat("vi-VN", {
    timeZone: "Asia/Ho_Chi_Minh",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  }).format(utcDate);
};

// Viết hoa chữ cái đầu tiên
export const capitalizeFirstWord = (str: string): string => {
  if (!str) return "";
  return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
};
