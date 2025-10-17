package com.taskmanagement.backend.dto;

import com.taskmanagement.backend.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ユーザーレスポンスDTO（Data Transfer Object）
 * 
 * このDTOの役割：
 * - サーバーからクライアントへユーザー情報を返す際に使用
 * - エンティティから必要な情報のみを抽出して送信
 * - **重要：パスワードは含めない**
 * 
 * なぜパスワードを含めないのか：
 * - セキュリティ上の理由で、パスワード（ハッシュ化済みでも）をAPIレスポンスに含めてはいけません
 * - クライアント側でパスワードを扱う必要は一切ありません
 * 
 * 実務でのポイント：
 * - ユーザー情報を返す際は、必ずこのDTOを使用します
 * - Userエンティティをそのまま返してはいけません（パスワードが含まれるため）
 * 
 * このDTOに含まれる情報：
 * - ID、メールアドレス、ユーザー名
 * - 作成日時、更新日時
 * 
 * このDTOに含まれない情報：
 * - パスワード（ハッシュ化済みでも含めない）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    /**
     * ユーザーID
     */
    private Long id;

    /**
     * メールアドレス
     */
    private String email;

    /**
     * ユーザー名
     */
    private String username;

    /**
     * アカウント作成日時
     */
    private LocalDateTime createdAt;

    /**
     * 最終更新日時
     */
    private LocalDateTime updatedAt;

    /**
     * Userエンティティから UserResponseDtoへの変換
     * 
     * 実務でのポイント：
     * - パスワードを含めないことが最も重要です
     * - この変換メソッドにより、安全にユーザー情報を返すことができます
     * 
     * 使用例：
     * User user = userRepository.findById(1L).orElseThrow();
     * UserResponseDto dto = UserResponseDto.fromEntity(user);
     * 
     * @param user Userエンティティ
     * @return UserResponseDto
     */
    public static UserResponseDto fromEntity(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        // 重要：パスワードは含めない！
        return dto;
    }
}