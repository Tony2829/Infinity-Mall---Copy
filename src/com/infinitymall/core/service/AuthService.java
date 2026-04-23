package com.infinitymall.core.service;

import com.infinitymall.core.model.*;
import com.infinitymall.core.ui.*;
import com.infinitymall.core.util.DataStore;
import java.util.Random;

/**
 * Authentication Service - Login, Signup, Password Reset
 */
public class AuthService {
    
    private static final Random RANDOM = new Random();
    
    /**
     * Show authentication menu
     */
    public static User showAuthMenu() {
        while (true) {
            UIHelper.clearScreen();
            
            // Splash screen
            String[] splash = {
                "",
                BrandColors.INFINITY_GOLD + BrandColors.BOLD,
                "██╗███╗   ██╗███████╗██╗███╗   ██╗██╗████████╗██╗   ██╗",
                "██║████╗  ██║██╔════╝██║████╗  ██║██║╚══██╔══╝╚██╗ ██╔╝",
                "██║██╔██╗ ██║█████╗  ██║██╔██╗ ██║██║   ██║    ╚████╔╝ ",
                "██║██║╚██╗██║██╔══╝  ██║██║╚██╗██║██║   ██║     ╚██╔╝  ",
                "██║██║ ╚████║██║     ██║██║ ╚████║██║   ██║      ██║   ",
                "╚═╝╚═╝  ╚═══╝╚═╝     ╚═╝╚═╝  ╚═══╝╚═╝   ╚═╝      ╚═╝   ",
                "",
                "███╗   ███╗ █████╗ ██╗     ██╗",
                "████╗ ████║██╔══██╗██║     ██║",
                "██╔████╔██║███████║██║     ██║",
                "██║╚██╔╝██║██╔══██║██║     ██║",
                "██║ ╚═╝ ██║██║  ██║███████╗███████╗",
                "╚═╝     ╚═╝╚═╝  ╚═╝╚══════╝╚══════╝",
                "",
                BrandColors.RESET
            };
            
            for (String line : splash) {
                UIHelper.printCentered(line, BrandColors.RESET);
                UIHelper.sleep(30);
            }
            
            System.out.println();
            UIHelper.printDivider("🔐 AUTHENTICATION", BrandColors.INFINITY_GOLD);
            System.out.println();
            
            String[] options = {
                "🔑 Login",
                "📝 Sign Up",
                "🔄 Forgot Password",
                "🚪 Exit"
            };
            
            UIHelper.printMenuOptions(options, BrandColors.GREEN);
            
            int choice = UIHelper.getIntInput("Select option", 1, 4);
            
            switch (choice) {
                case 1:
                    User user = login();
                    if (user != null) return user;
                    break;
                case 2:
                    User newUser = signup();
                    if (newUser != null) return newUser;
                    break;
                case 3:
                    forgotPassword();
                    break;
                case 4:
                    UIHelper.clearScreen();
                    UIHelper.typeWriteCentered("👋 Thank you for visiting Infinity Mall!", 
                                              BrandColors.CYAN, 40);
                    UIHelper.sleep(1500);
                    System.exit(0);
                    break;
            }
        }
    }
    
    /**
     * Login flow
     */
    private static User login() {
        UIHelper.clearScreen();
        UIHelper.printHeader("LOGIN", "🔑", BrandColors.CYAN);
        
        System.out.println();
        String email = UIHelper.getInput("📧 Email");
        String password = UIHelper.getPassword("🔒 Password");
        
        UIHelper.loadingAnimation("🔐 Authenticating");
        
        DataStore store = DataStore.getInstance();
        User user = store.getUserByEmail(email);
        
        if (user != null && user.getPassword().equals(password)) {
            user.setLoggedIn(true);
            store.addActiveSession(user.getUserId());
            
            UIHelper.showSuccess("Login Successful!");
            UIHelper.typeWriteCentered("👋 Welcome back, " + user.getFullName() + "!", 
                                      BrandColors.CYAN, 40);
            UIHelper.sleep(1500);
            return user;
        } else {
            UIHelper.showError("Invalid email or password!");
            UIHelper.pressAnyKeyToContinue();
            return null;
        }
    }
    
    /**
     * Signup flow
     */
    private static User signup() {
        UIHelper.clearScreen();
        UIHelper.printHeader("CREATE ACCOUNT", "📝", BrandColors.GREEN);
        
        System.out.println();
        String fullName = UIHelper.getInput("👤 Full Name");
        String email = UIHelper.getInput("📧 Email");
        
        // Check if email exists
        if (DataStore.getInstance().getUserByEmail(email) != null) {
            UIHelper.showError("Email already registered!");
            UIHelper.pressAnyKeyToContinue();
            return null;
        }
        
        String phone = UIHelper.getInput("📱 Phone");
        String password = UIHelper.getPassword("🔒 Password");
        
        // Generate OTP
        int otp = generateOTP();
        System.out.println();
        UIHelper.printBox("OTP VERIFICATION", 
            new String[]{
                "📱 OTP sent to " + phone,
                "🔐 Your OTP is: " + BrandColors.YELLOW + otp + BrandColors.RESET,
                "⚠️ DO NOT share this with anyone"
            }, BrandColors.INFINITY_GOLD);
        
        int enteredOtp = UIHelper.getIntInput("Enter OTP", 1000, 9999);
        
        if (enteredOtp == otp) {
            User newUser = new User(fullName, email, phone, password);
            newUser.setLoggedIn(true);
            
            DataStore.getInstance().addUser(newUser);
            DataStore.getInstance().addActiveSession(newUser.getUserId());
            
            UIHelper.showSuccess("Account Created Successfully!");
            UIHelper.typeWriteCentered("🎉 Welcome to Infinity Mall, " + fullName + "!", 
                                      BrandColors.GREEN, 40);
            UIHelper.sleep(1500);
            return newUser;
        } else {
            UIHelper.showError("Invalid OTP! Registration failed.");
            UIHelper.pressAnyKeyToContinue();
            return null;
        }
    }
    
    /**
     * Forgot password flow
     */
    private static void forgotPassword() {
        UIHelper.clearScreen();
        UIHelper.printHeader("RESET PASSWORD", "🔄", BrandColors.YELLOW);
        
        System.out.println();
        String phone = UIHelper.getInput("📱 Enter Registered Phone");
        
        User user = DataStore.getInstance().getUserByPhone(phone);
        
        if (user == null) {
            UIHelper.showError("No account found with this phone!");
            UIHelper.pressAnyKeyToContinue();
            return;
        }
        
        int otp = generateOTP();
        System.out.println();
        UIHelper.printBox("OTP VERIFICATION", 
            new String[]{
                "📱 OTP sent to " + phone,
                "🔐 Your OTP is: " + BrandColors.YELLOW + otp + BrandColors.RESET
            }, BrandColors.INFINITY_GOLD);
        
        int enteredOtp = UIHelper.getIntInput("Enter OTP", 1000, 9999);
        
        if (enteredOtp == otp) {
            String newPassword = UIHelper.getPassword("🔒 New Password");
            user.setPassword(newPassword);
            
            UIHelper.showSuccess("Password Reset Successful!");
            UIHelper.typeWriteCentered("🔐 You can now login with your new password", 
                                      BrandColors.GREEN, 30);
            UIHelper.sleep(1500);
        } else {
            UIHelper.showError("Invalid OTP!");
            UIHelper.pressAnyKeyToContinue();
        }
    }
    
    /**
     * Generate 4-digit OTP
     */
    private static int generateOTP() {
        return 1000 + RANDOM.nextInt(9000);
    }
}