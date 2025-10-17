package com.taskmanagement.backend.repository;

import com.taskmanagement.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepositoryインターフェース
 * 
 * Spring Data JPAのJpaRepositoryを継承することで、
 * 基本的なCRUD操作が自動的に提供されます。
 * 
 * 実務でのポイント：
 * - インターフェースを定義するだけで、実装クラスは不要です
 * - Spring Data JPAが実行時に自動的に実装クラスを生成します
 * - これにより、ボイラープレートコードを大幅に削減できます
 * 
 * 自動提供されるメソッド（一部）：
 * - save(User user): ユーザーを保存または更新
 * - findById(Long id): IDでユーザーを検索
 * - findAll(): すべてのユーザーを取得
 * - deleteById(Long id): IDでユーザーを削除
 * - count(): ユーザー数をカウント
 * 
 * カスタムクエリメソッド：
 * - メソッド名のルールに従うことで、自動的にクエリが生成されます
 * - 例: findByEmail → "SELECT u FROM User u WHERE u.email = :email"
 * 
 * @Repository: このインターフェースがリポジトリであることを示します
 * （Spring Data JPAでは@Repositoryは必須ではありませんが、明示的に付けることを推奨）
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * メールアドレスでユーザーを検索
     * 
     * 実務での使用場面：
     * - ログイン処理で、メールアドレスからユーザーを取得
     * - ユーザー登録時に、メールアドレスの重複チェック
     * 
     * メソッド名のルール：
     * - findBy + フィールド名（先頭大文字）
     * - "findByEmail" → "WHERE email = ?"
     * - "findByEmailAndPassword" → "WHERE email = ? AND password = ?"
     * 
     * Optional<User>を返す理由：
     * - ユーザーが見つからない場合にnullを返すのではなく、Optionalを返します
     * - これにより、NullPointerExceptionを防ぎ、安全にコードを書けます
     * 
     * 使用例：
     * Optional<User> userOpt = userRepository.findByEmail("test@example.com");
     * if (userOpt.isPresent()) {
     *     User user = userOpt.get();
     *     // ユーザーが見つかった場合の処理
     * } else {
     *     // ユーザーが見つからない場合の処理
     * }
     * 
     * @param email メールアドレス
     * @return ユーザー（見つからない場合はOptional.empty()）
     */
    Optional<User> findByEmail(String email);

    /**
     * メールアドレスの存在確認
     * 
     * 実務での使用場面：
     * - ユーザー登録時に、メールアドレスがすでに使用されているかチェック
     * 
     * メソッド名のルール：
     * - existsBy + フィールド名（先頭大文字）
     * - "existsByEmail" → "SELECT COUNT(*) > 0 FROM User WHERE email = ?"
     * 
     * booleanを返す理由：
     * - 存在確認なので、true/falseで十分です
     * - findByEmailよりも効率的（データを取得せず、存在確認のみ）
     * 
     * 使用例：
     * if (userRepository.existsByEmail("test@example.com")) {
     *     throw new IllegalArgumentException("このメールアドレスは既に使用されています");
     * }
     * 
     * @param email メールアドレス
     * @return 存在する場合はtrue、存在しない場合はfalse
     */
    boolean existsByEmail(String email);

    /**
     * ユーザー名でユーザーを検索
     * 
     * 実務での使用場面：
     * - ユーザー検索機能
     * - ユーザー一覧の絞り込み
     * 
     * 注意点：
     * - ユーザー名は一意ではないため、複数のユーザーが見つかる可能性があります
     * - そのため、Optional<User>ではなく、List<User>を返します
     * 
     * 部分一致検索の場合：
     * - findByUsernameContaining(String username)
     * - "findByUsernameContaining" → "WHERE username LIKE '%?%'"
     * 
     * @param username ユーザー名
     * @return ユーザーのリスト（見つからない場合は空のリスト）
     */
    Optional<User> findByUsername(String username);
}