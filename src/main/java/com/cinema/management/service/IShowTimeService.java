package com.cinema.management.service;

import com.cinema.management.model.entity.ShowTime;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interface định nghĩa các nghiệp vụ quản lý suất chiếu.
 */
public interface IShowTimeService {

    List<ShowTime> getAllShowTimes();

    List<ShowTime> getShowTimesByMovie(String movieId);

    Optional<ShowTime> getShowTimeById(String showTimeId);

    /**
     * Thêm suất chiếu. Validate: endTime > startTime, không xung đột lịch phòng.
     * @throws IllegalArgumentException nếu thời gian không hợp lệ hoặc xung đột lịch
     */
    ShowTime addShowTime(String movieId, String roomId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Cập nhật suất chiếu. Validate tương tự addShowTime (loại trừ chính nó).
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    ShowTime updateShowTime(String showTimeId, String movieId, String roomId,
                            LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Xóa suất chiếu. Chỉ xóa được nếu chưa có booking liên kết.
     * @throws IllegalStateException nếu suất chiếu đã có vé bán
     */
    void deleteShowTime(String showTimeId);
}
