package com.taskmanagement.backend.service;

import com.taskmanagement.backend.dto.UserResponseDto;
import com.taskmanagement.backend.model.User;
import com.taskmanagement.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * ユーザーサービス
 * 
 * Serviceレイヤーの役割：
 * - ビジネスロジックを実装
 * - Repository層とController層の橋渡し
 * - トランザクション管理
 * - DTOとエンティティの変換
 * 
 * 実務でのポイント：
 * - Controllerはリクエスト/レスポンスの処理のみを行い、
 * ビジネスロジックはServiceに委譲します
 * - Repositoryはデータベースアクセスのみを行い、
 * ビジネスロジックはServiceに委譲します
 * - これにより、関心の分離（Separation of Concerns）が実現されます
 * 
 * @Service:
 *           - このクラスがServiceレイヤーのコンポーネントであることを示します
 *           - Spring Bootが自動的にBeanとして登録します
 * 
 * @RequiredArgsConstructor (Lombok):
 *                          - finalフィールドを引数に持つコンストラクタを自動生成します
 *                          - これにより、依存性注入（DI）が簡潔に記述できます
 * 
 * @Transactional:
 *                 - トランザクション管理を自動化します
 *                 - メソッド実行中にエラーが発生した場合、データベースの変更がロールバックされます
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    /**
     * ユーザーリポジトリ
     * 
     * 実務でのポイント：
     * - finalで宣言することで、不変性を保証します
     * - @RequiredArgsConstructorにより、コンストラクタインジェクションが自動化されます
     */
    private final UserRepository userRepository;

    /**
     * IDでユーザーを取得
     * 
     * 実務でのポイント：
     * - UserResponseDtoを返すことで、パスワードを含めずに安全に返します
     * - Optional<T>を使用することで、nullセーフに実装できます
     * 
     * @param id ユーザーID
     * @return UserResponseDto（見つからない場合はOptional.empty()）
     */
    public Optional<UserResponseDto> findById(Long id) {
        return userRepository.findById(id)
                .map(UserResponseDto::fromEntity);
    }

    /**
     * メールアドレスでユーザーを取得
     * 
     * 実務での使用場面：
     * - ログイン処理
     * - ユーザー検索
     * 
     * @param email メールアドレス
     * @return UserResponseDto（見つからない場合はOptional.empty()）
     */
    public Optional<UserResponseDto> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserResponseDto::fromEntity);
    }

    /**
     * メールアドレスの存在確認
     * 
     * 実務での使用場面：
     * - ユーザー登録時の重複チェック
     * 
     * @param email メールアドレス
     * @return 存在する場合はtrue、存在しない場合はfalse
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * ユーザーを作成
     * 
     * @Transactional (readOnly = false):
     *                - クラスレベルでは readOnly = true ですが、
     *                このメソッドでは readOnly = false にすることで、書き込みを許可します
     * 
     *                実務でのポイント：
     *                - パスワードは既にハッシュ化されている前提です
     *                - パスワードのハッシュ化は、Controllerまたは別のServiceで行います
     *                - メールアドレスの重複チェックは、Controllerで行うことを推奨します
     * 
     * @param email    メールアドレス
     * @param password パスワード（ハッシュ化済み）
     * @param username ユーザー名
     * @return 作成されたユーザー（UserResponseDto）
     * @throws IllegalArgumentException メールアドレスが既に使用されている場合
     */
    @Transactional
    public UserResponseDto createUser(String email, String password, String username) {
        // メールアドレスの重複チェック
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("このメールアドレスは既に使用されています");
        }

        // ユーザーエンティティを作成
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setUsername(username);

        // データベースに保存
        User savedUser = userRepository.save(user);

        // DTOに変換して返す
        return UserResponseDto.fromEntity(savedUser);
    }

    /**
     * ユーザー情報を更新
     * 
     * 実務でのポイント：
     * - パスワードの更新は、別メソッド（updatePassword）で行うことを推奨します
     * - これにより、パスワード変更時に追加のセキュリティチェックを行えます
     * 
     * @param id       ユーザーID
     * @param username 新しいユーザー名
     * @return 更新されたユーザー（UserResponseDto）
     * @throws IllegalArgumentException ユーザーが見つからない場合
     */
    @Transactional
    public UserResponseDto updateUser(Long id, String username) {
        // ユーザーを取得
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));

        // ユーザー名を更新
        user.setUsername(username);

        // データベースに保存（@PreUpdateが自動実行され、updatedAtが更新される）
        User updatedUser = userRepository.save(user);

        // DTOに変換して返す
        return UserResponseDto.fromEntity(updatedUser);
    }

    /**
     * パスワードを更新
     * 
     * 実務でのポイント：
     * - パスワードは既にハッシュ化されている前提です
     * - パスワードのハッシュ化は、Controllerまたは別のServiceで行います
     * - 現在のパスワードの確認は、Controllerで行うことを推奨します
     * 
     * @param id          ユーザーID
     * @param newPassword 新しいパスワード（ハッシュ化済み）
     * @return 更新されたユーザー（UserResponseDto）
     * @throws IllegalArgumentException ユーザーが見つからない場合
     */
    @Transactional
    public UserResponseDto updatePassword(Long id, String newPassword) {
        // ユーザーを取得
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));

        // パスワードを更新
        user.setPassword(newPassword);

        // データベースに保存
        User updatedUser = userRepository.save(user);

        // DTOに変換して返す
        return UserResponseDto.fromEntity(updatedUser);
    }

    /**
     * ユーザーを削除
     * 
     * 実務でのポイント：
     * - 論理削除（削除フラグを立てる）と物理削除（データベースから削除）の選択
     * - 今回は物理削除を実装していますが、実務では論理削除が一般的です
     * - 論理削除の場合は、deletedフラグやdeletedAtフィールドを追加します
     * 
     * @param id ユーザーID
     * @throws IllegalArgumentException ユーザーが見つからない場合
     */
    @Transactional
    public void deleteUser(Long id) {
        // ユーザーが存在するか確認
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("ユーザーが見つかりません");
        }

        // ユーザーを削除
        userRepository.deleteById(id);
    }

    /**
     * ユーザー総数を取得
     * 
     * 実務での使用場面：
     * - 管理画面での統計情報表示
     * 
     * @return ユーザー総数
     */
    public long countUsers() {
        return userRepository.count();
    }
}