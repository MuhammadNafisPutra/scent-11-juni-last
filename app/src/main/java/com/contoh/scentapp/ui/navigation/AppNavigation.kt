package com.contoh.scentapp.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import com.contoh.scentapp.data.model.Routes
import com.contoh.scentapp.data.model.Order
import com.contoh.scentapp.data.model.OrderStatus
import com.contoh.scentapp.data.repository.CartRepository
import com.contoh.scentapp.data.repository.OrderRepositoryImpl
import com.contoh.scentapp.data.repository.SessionManager
import com.contoh.scentapp.ui.auth.LoginScreen
import com.contoh.scentapp.ui.auth.RegisterScreen
import com.contoh.scentapp.ui.cart.CartScreen
import com.contoh.scentapp.ui.cart.UploadPaymentProofScreen
import com.contoh.scentapp.ui.detail.DetailScreen
import com.contoh.scentapp.ui.favorite.FavoriteScreen
import com.contoh.scentapp.ui.home.HomeScreen
import com.contoh.scentapp.ui.order.OrderDetailScreen
import com.contoh.scentapp.ui.order.OrderHistoryScreen
import com.contoh.scentapp.ui.ordersuccess.OrderSuccessScreen
import com.contoh.scentapp.ui.profile.AccountDetailScreen
import com.contoh.scentapp.ui.profile.LanguageScreen
import com.contoh.scentapp.ui.profile.ProfileScreen
import com.contoh.scentapp.ui.profile.ShippingAddressScreen
import com.contoh.scentapp.ui.review.AddReviewScreen
import com.contoh.scentapp.ui.sales.AddProductScreen
import com.contoh.scentapp.ui.sales.SalesScreen
import com.contoh.scentapp.ui.sales.SalesViewModel
import com.contoh.scentapp.ui.sales.SalesViewModelFactory
import com.contoh.scentapp.ui.sales.SellerOrderDetailScreen
import com.contoh.scentapp.ui.search.SearchScreen
import com.contoh.scentapp.ui.shipping.ShippingScreen
import com.contoh.scentapp.ui.theme.components.ScentBottomNavBar

private val bottomNavRoutes = setOf(
    Routes.HOME, Routes.FAVORITE, Routes.CART, Routes.PROFILE
)

@Composable
fun AppNavigation(startLoggedIn: Boolean = false) {
    val context        = LocalContext.current
    val navController  = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination?.route ?: Routes.LOGIN

    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                ScentBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate   = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        val salesViewModel: SalesViewModel = viewModel(factory = SalesViewModelFactory())
        val coroutineScope  = rememberCoroutineScope()
        val cartRepository  = CartRepository.getInstance()
        val orderRepository = OrderRepositoryImpl()

        // ── Buat dokumen pesanan dari isi keranjang saat checkout ──────────────
        // Dipanggil baik dari jalur COD (ShippingScreen) maupun Transfer
        // (UploadPaymentProofScreen), agar pesanan langsung tercatat di
        // Firestore dan muncul di Riwayat Pesanan (buyer) & Pesanan Masuk (seller).
        fun createOrdersFromCart(isTransfer: Boolean, onDone: () -> Unit) {
            coroutineScope.launch {
                val items   = cartRepository.cartItems.first()
                val summary = cartRepository.checkoutSummary.first()

                if (items.isEmpty()) {
                    onDone()
                    return@launch
                }

                val initialStatus = if (isTransfer) {
                    OrderStatus.MENUNGGU_KONFIRMASI
                } else {
                    OrderStatus.WAITING_PAYMENT
                }
                val paymentMethodLabel = if (isTransfer) "Transfer" else "COD"

                // Pisahkan item per penjual (sellerId) — satu order Firestore
                // hanya boleh memiliki satu sellerId agar muncul benar di
                // halaman "Pesanan Masuk" milik penjual tersebut.
                val itemsBySeller = items.groupBy { it.sellerId }

                itemsBySeller.entries.forEachIndexed { index, (sellerId, sellerItems) ->
                    val subtotal = sellerItems.sumOf { it.totalPrice }
                    // Biaya kirim dibebankan pada order pertama saja agar
                    // total yang ditampilkan saat checkout tetap sesuai.
                    val shippingForThisOrder = if (index == 0) summary.shippingFee else 0

                    orderRepository.createOrder(
                        Order(
                            sellerId      = sellerId,
                            items         = sellerItems,
                            totalPrice    = subtotal.toLong(),
                            shippingCost  = shippingForThisOrder.toLong(),
                            paymentMethod = paymentMethodLabel,
                            status        = initialStatus
                        )
                    )
                }

                cartRepository.clearCart()
                onDone()
            }
        }

        NavHost(
            navController    = navController,
            startDestination = if (startLoggedIn) Routes.HOME else Routes.LOGIN,
            modifier         = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onRegister = { navController.navigate(Routes.REGISTER) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onLogin = { navController.popBackStack() }
                )
            }
            composable(Routes.HOME) {
                HomeScreen(
                    // ← passing firestoreId (String) bukan productId (Int)
                    onProductClick = { firestoreId ->
                        navController.navigate(Routes.detailRoute(firestoreId))
                    },
                    onSearchClick = {
                        navController.navigate(Routes.searchRoute())
                    }
                )
            }
            composable(Routes.FAVORITE) {
                FavoriteScreen(
                    onBack         = { navController.popBackStack() },
                    // ← passing firestoreId (String)
                    onProductClick = { firestoreId ->
                        navController.navigate(Routes.detailRoute(firestoreId))
                    }
                )
            }
            composable(Routes.CART) {
                CartScreen(
                    onBack             = { navController.popBackStack() },
                    onCheckout         = { navController.navigate(Routes.SHIPPING) },
                    onContinueShopping = { navController.navigate(Routes.HOME) }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onBack           = { navController.popBackStack() },
                    onDetailAkun     = { navController.navigate(Routes.ACCOUNT_DETAIL) },
                    onAlamat         = { navController.navigate(Routes.SHIPPING_ADDRESS) },
                    onRiwayatPesanan = { navController.navigate(Routes.ORDER_HISTORY) },
                    onBahasa         = { navController.navigate(Routes.LANGUAGE) },
                    onPenjualan      = { navController.navigate(Routes.SALES) },
                    onLogout         = {
                        SessionManager.getInstance(context).clearSession()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route     = Routes.DETAIL,
                // ← ganti IntType → StringType, ganti key "productId" → "firestoreId"
                arguments = listOf(navArgument("firestoreId") { type = NavType.StringType })
            ) { backStack ->
                val firestoreId = backStack.arguments?.getString("firestoreId")
                    ?: return@composable
                DetailScreen(
                    firestoreId      = firestoreId,
                    onBack           = { navController.popBackStack() },
                    onNavigateToCart = { navController.navigate(Routes.CART) },
                    onWriteReview    = { navController.navigate(Routes.addReviewRoute(firestoreId)) }
                )
            }
            composable(
                route     = Routes.SEARCH,
                arguments = listOf(
                    navArgument("query") {
                        type         = NavType.StringType
                        defaultValue = ""
                        nullable     = false
                    }
                )
            ) { backStack ->
                SearchScreen(
                    initialQuery   = backStack.arguments?.getString("query") ?: "",
                    onBack         = { navController.popBackStack() },
                    // ← passing firestoreId (String)
                    onProductClick = { firestoreId ->
                        navController.navigate(Routes.detailRoute(firestoreId))
                    }
                )
            }
            composable(Routes.SHIPPING) {
                ShippingScreen(
                    onBack    = { navController.popBackStack() },
                    onConfirm = { isTransfer ->
                        if (isTransfer) {
                            navController.navigate(Routes.UPLOAD_BUKTI)
                        } else {
                            createOrdersFromCart(isTransfer = false) {
                                navController.navigate(Routes.orderSuccessRoute(false)) {
                                    popUpTo(Routes.CART) { inclusive = true }
                                }
                            }
                        }
                    }
                )
            }
            composable(Routes.UPLOAD_BUKTI) {
                UploadPaymentProofScreen(
                    onBack   = { navController.popBackStack() },
                    onSubmit = {
                        createOrdersFromCart(isTransfer = true) {
                            navController.navigate(Routes.orderSuccessRoute(true)) {
                                popUpTo(Routes.CART) { inclusive = true }
                            }
                        }
                    }
                )
            }
            composable(
                route     = Routes.ORDER_SUCCESS,
                arguments = listOf(navArgument("isTransfer") { type = NavType.BoolType })
            ) { backStack ->
                val isTransfer = backStack.arguments?.getBoolean("isTransfer") ?: false
                OrderSuccessScreen(
                    isTransfer = isTransfer,
                    onBackHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(navController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Routes.ACCOUNT_DETAIL) {
                AccountDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SHIPPING_ADDRESS) {
                ShippingAddressScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.LANGUAGE) {
                LanguageScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SALES) {
                SalesScreen(
                    onBack       = { navController.popBackStack() },
                    onAddProduct = { navController.navigate(Routes.ADD_PRODUCT) },
                    onOrderClick = { orderId ->
                        navController.navigate(Routes.sellerOrderDetailRoute(orderId))
                    },
                    viewModel    = salesViewModel
                )
            }
            composable(Routes.ADD_PRODUCT) {
                AddProductScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.ORDER_HISTORY) {
                OrderHistoryScreen(
                    onBack             = { navController.popBackStack() },
                    onOrderDetailClick = { orderId ->
                        navController.navigate(Routes.orderDetailRoute(orderId))
                    }
                )
            }
            composable(
                route     = Routes.ORDER_DETAIL,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStack ->
                val orderId = backStack.arguments?.getString("orderId") ?: ""
                OrderDetailScreen(
                    orderId = orderId,
                    onBack  = { navController.popBackStack() }
                )
            }
            composable(
                route     = Routes.SELLER_ORDER_DETAIL,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStack ->
                val orderId = backStack.arguments?.getString("orderId") ?: ""
                SellerOrderDetailScreen(
                    orderId = orderId,
                    onBack  = { navController.popBackStack() }
                )
            }
            composable(
                route     = Routes.ADD_REVIEW,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStack ->
                val orderId = backStack.arguments?.getString("orderId") ?: ""
                AddReviewScreen(
                    orderId = orderId,
                    onBack  = { navController.popBackStack() }
                )
            }
        }
    }
}