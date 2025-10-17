package com.taskmanagement.backend.controller;

import com.taskmanagement.backend.dto.UserResponseDto;
import com.taskmanagement.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserControllerのテスト
 * 
 * @WebMvcTest:
 *              - Controller層のみをテストします
 *              - Service層やRepository層は含まれません
 *              - MockMvcを使用してHTTPリクエストをシミュレートします
 * 
 * @AutoConfigureMockMvc(addFilters = false):
 *                                  - Spring Securityのフィルタを無効化します
 *                                  - Phase 2-4の段階では、認証機能をまだ実装していないため、
 *                                  テストでSpring Securityのデフォルト設定を無効化します
 *                                  - Phase
 *                                  2-5で認証機能を実装した後は、@WithMockUserなどを使用します
 * 
 * @MockBean:
 *            - Service層をモック化します
 *            - 実際のService層は動作せず、モックの動作を定義します
 * 
 *            MockMvcとは：
 *            - HTTPリクエストをシミュレートするためのツール
 *            - 実際のHTTPサーバーを起動せずにテストできます
 *            - perform()でリクエストを送信し、andExpect()でレスポンスを検証します
 * 
 *            実務でのポイント：
 *            - Controller層のテストでは、ビジネスロジックをテストしません
 *            - HTTPリクエスト/レスポンスの処理をテストします
 *            - Service層の動作はモックで定義します
 * 
 *            Spring Securityとテストの注意点：
 *            - Spring Securityが有効な場合、デフォルトですべてのエンドポイントが認証を要求します
 *            - テストでは、@AutoConfigureMockMvc(addFilters = false)でフィルタを無効化するか、
 * @WithMockUserで認証情報を含める必要があります
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
     */
    @Test
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
    void testDeleteUser() throws Exception {
        // HTTPリクエストを送信し、レスポンスを検証
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent()); // HTTPステータスコード204
    }

    /**
     * ユーザー総数を取得するテスト
     */
    @Test
    void testCountUsers() throws Exception {
        // モックの動作を定義
        when(userService.countUsers()).thenReturn(42L);

        // HTTPリクエストを送信し、レスポンスを検証
        mockMvc.perform(get("/api/users/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("42"));
    }
}