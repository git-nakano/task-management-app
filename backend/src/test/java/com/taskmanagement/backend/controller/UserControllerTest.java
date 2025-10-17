package com.taskmanagement.backend.controller;

import com.taskmanagement.backend.dto.UserResponseDto;
import com.taskmanagement.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserControllerのテスト（Phase 2-5版）
 * 
 * @WebMvcTest:
 *              - Controller層のみをテストします
 *              - Service層やRepository層は含まれません
 *              - MockMvcを使用してHTTPリクエストをシミュレートします
 * 
 *              Phase 2-5での更新：
 *              - @AutoConfigureMockMvc(addFilters = false)を追加しました
 *              - これにより、テスト時にSpring Securityのフィルタが無効化されます
 *              - 認証なしでAPIにアクセスできるため、テストが簡潔になります
 * 
 *              なぜaddFilters = falseが必要か：
 *              - Phase 2-5でSpring Securityを有効化しました
 *              - SecurityConfigで、/api/auth/**以外のすべてのエンドポイントに認証が必要になりました
 *              - テストでは認証情報を提供する代わりに、フィルタを無効化することでテストを簡素化します
 * 
 *              実務でのポイント：
 *              - テスト時にSpring Securityのフィルタを無効化することは、一般的な手法です
 *              - Controller層のテストでは、ビジネスロジックに焦点を当てるため、認証はスキップします
 *              - 認証のテストは、別途統合テストで行います（Phase 2-6で実装予定）
 */
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private UserService userService;

        /**
         * IDでユーザーを取得するテスト（成功）
         * 
         * @WithMockUser:
         *                - モックユーザーでログインした状態をシミュレートします
         *                - username: テストユーザーのメールアドレス
         *                - roles: ユーザーの役割（USER）
         */
        @Test
        @WithMockUser(username = "test@example.com", roles = "USER")
        void testGetUserById_Success() throws Exception {
                // モックの動作を定義
                UserResponseDto userDto = new UserResponseDto(
                                1L,
                                "test@example.com",
                                "テストユーザー",
                                LocalDateTime.now(),
                                LocalDateTime.now());
                when(userService.findById(1L)).thenReturn(Optional.of(userDto));

                // HTTPリクエストを送信し、レスポンスを検証
                mockMvc.perform(get("/api/users/1"))
                                .andExpect(status().isOk()) // HTTPステータスコード200
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.email").value("test@example.com"))
                                .andExpect(jsonPath("$.username").value("テストユーザー"));
        }

        /**
         * IDでユーザーを取得するテスト（見つからない）
         */
        @Test
        @WithMockUser(username = "test@example.com", roles = "USER")
        void testGetUserById_NotFound() throws Exception {
                // モックの動作を定義：ユーザーが見つからない
                when(userService.findById(999L)).thenReturn(Optional.empty());

                // HTTPリクエストを送信し、レスポンスを検証
                mockMvc.perform(get("/api/users/999"))
                                .andExpect(status().isNotFound()); // HTTPステータスコード404
        }

        /**
         * メールアドレスでユーザーを取得するテスト
         */
        @Test
        @WithMockUser(username = "test@example.com", roles = "USER")
        void testGetUserByEmail_Success() throws Exception {
                // モックの動作を定義
                UserResponseDto userDto = new UserResponseDto(
                                1L,
                                "test@example.com",
                                "テストユーザー",
                                LocalDateTime.now(),
                                LocalDateTime.now());
                when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(userDto));

                // HTTPリクエストを送信し、レスポンスを検証
                mockMvc.perform(get("/api/users/email/test@example.com"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value("test@example.com"));
        }

        /**
         * ユーザーを作成するテスト
         */
        @Test
        @WithMockUser(username = "test@example.com", roles = "USER")
        void testCreateUser() throws Exception {
                // モックの動作を定義
                UserResponseDto userDto = new UserResponseDto(
                                1L,
                                "new@example.com",
                                "新しいユーザー",
                                LocalDateTime.now(),
                                LocalDateTime.now());
                when(userService.createUser(anyString(), anyString(), anyString())).thenReturn(userDto);

                // HTTPリクエストを送信し、レスポンスを検証
                mockMvc.perform(post("/api/users")
                                .param("email", "new@example.com")
                                .param("password", "password")
                                .param("username", "新しいユーザー"))
                                .andExpect(status().isCreated()) // HTTPステータスコード201
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.email").value("new@example.com"))
                                .andExpect(jsonPath("$.username").value("新しいユーザー"));
        }

        /**
         * ユーザー情報を更新するテスト
         */
        @Test
        @WithMockUser(username = "test@example.com", roles = "USER")
        void testUpdateUser() throws Exception {
                // モックの動作を定義
                UserResponseDto userDto = new UserResponseDto(
                                1L,
                                "test@example.com",
                                "更新されたユーザー",
                                LocalDateTime.now(),
                                LocalDateTime.now());
                when(userService.updateUser(eq(1L), anyString())).thenReturn(userDto);

                // HTTPリクエストを送信し、レスポンスを検証
                mockMvc.perform(put("/api/users/1")
                                .param("username", "更新されたユーザー"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.username").value("更新されたユーザー"));
        }

        /**
         * ユーザーを削除するテスト
         */
        @Test
        @WithMockUser(username = "test@example.com", roles = "USER")
        void testDeleteUser() throws Exception {
                // HTTPリクエストを送信し、レスポンスを検証
                mockMvc.perform(delete("/api/users/1"))
                                .andExpect(status().isNoContent()); // HTTPステータスコード204
        }

        /**
         * ユーザー総数を取得するテスト
         */
        @Test
        @WithMockUser(username = "test@example.com", roles = "USER")
        void testCountUsers() throws Exception {
                // モックの動作を定義
                when(userService.countUsers()).thenReturn(42L);

                // HTTPリクエストを送信し、レスポンスを検証
                mockMvc.perform(get("/api/users/count"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("42"));
        }
}