-- phpMyAdmin SQL Dump
-- Unified PharmaX Database
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `pharmax`
--
DROP TABLE IF EXISTS `admin_log`;
--
-- Table structure for table `admin_log`
--

CREATE TABLE `admin_log` (
  `id` int(11) NOT NULL,
  `admin_id` int(11) NOT NULL,
  `action_type` varchar(50) NOT NULL,
  `target_id` int(11) DEFAULT NULL,
  `details` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `admin_log`
--

INSERT INTO `admin_log` (`id`, `admin_id`, `action_type`, `target_id`, `details`, `created_at`) VALUES
(1, 3, 'UPDATE_USER', 12, 'Updated user: amal.aguir@esprit.tn', '2026-04-30 00:37:09'),
(2, 3, 'DELETE_USER', 13, 'Deleted user with ID: 13', '2026-04-30 00:39:53'),
(3, 3, 'UPDATE_STATUS', 12, 'Changed status to: BLOCKED', '2026-04-30 00:58:15'),
(4, 3, 'UPDATE_STATUS', 12, 'Changed status to: UNBLOCKED', '2026-04-30 00:58:27'),
(5, 3, 'UPDATE_USER', 3, 'Updated user: lola.aguir@gmail.com', '2026-04-30 02:10:02'),
(6, 3, 'UPDATE_USER', 3, 'Updated user: lola.aguir@gmail.com', '2026-04-30 02:10:09'),
(7, 3, 'UPDATE_STATUS', 12, 'Changed status to: BLOCKED', '2026-04-30 02:13:40'),
(8, 3, 'UPDATE_STATUS', 12, 'Changed status to: UNBLOCKED', '2026-04-30 02:13:44'),
(9, 3, 'CREATE_USER', 16, 'Created user: test@gmail.com', '2026-04-30 09:24:09'),
(10, 3, 'UPDATE_USER', 3, 'Updated user: lola.aguir@gmail.com', '2026-04-30 09:24:36'),
(11, 3, 'UPDATE_USER', 3, 'Updated user: lola.aguir@gmail.com', '2026-04-30 09:24:41'),
(12, 3, 'UPDATE_USER', 3, 'Updated user: lola.aguir@gmail.com', '2026-04-30 09:24:46'),
(13, 3, 'UPDATE_USER', 3, 'Updated user: lola.aguir@gmail.com', '2026-04-30 09:35:40'),
(14, 3, 'UPDATE_USER', 3, 'Updated user: lola.aguir@gmail.com', '2026-04-30 09:35:55'),
(15, 3, 'UPDATE_USER', 3, 'Updated user: lola.aguir@gmail.com', '2026-04-30 09:36:08'),
(16, 3, 'DELETE_USER', 16, 'Deleted user with ID: 16', '2026-04-30 10:10:53'),
(17, 3, 'UPDATE_USER', 3, 'Updated user: lola.aguir@gmail.com', '2026-04-30 10:12:50');

-- --------------------------------------------------------

DROP TABLE IF EXISTS `chat_message`;
--
-- Table structure for table `chat_message`
--

CREATE TABLE `chat_message` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `role` varchar(20) NOT NULL,
  `content` text NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `chat_message`
--

INSERT INTO `chat_message` (`id`, `user_id`, `role`, `content`, `created_at`) VALUES
(1, 3, 'user', 'Delete the user called dummy', '2026-04-30 00:38:28'),
(2, 3, 'model', '❌ Error: OpenRouter API error (HTTP 400): {\"error\":{\"message\":\"google/gemini-2.0-pro-exp-02-05:free is not a valid model ID\",\"code\":400},\"user_id\":\"user_3D3D1mTHTjUJpjiwvajRfjcA20h\"}', '2026-04-30 00:38:28'),
(3, 3, 'user', 'Hello', '2026-04-30 00:48:22'),
(4, 3, 'model', '❌ Error: OpenRouter API error (HTTP 404): {\"error\":{\"message\":\"No endpoints found for mistralai/mistral-7b-instruct:free.\",\"code\":404},\"user_id\":\"user_3D3D1mTHTjUJpjiwvajRfjcA20h\"}', '2026-04-30 00:48:24'),
(5, 3, 'user', 'Hello?', '2026-04-30 00:52:33'),
(6, 3, 'model', '❌ Error: OpenRouter API error (HTTP 404): {\"error\":{\"message\":\"No endpoints found for meta-llama/llama-3-8b-instruct:free.\",\"code\":404},\"user_id\":\"user_3D3D1mTHTjUJpjiwvajRfjcA20h\"}', '2026-04-30 00:52:33'),
(7, 3, 'user', '.', '2026-04-30 00:54:30'),
(8, 3, 'model', '❌ Error: OpenRouter API error (HTTP 404): {\"error\":{\"message\":\"No endpoints found for deepseek/deepseek-r1:free.\",\"code\":404},\"user_id\":\"user_3D3D1mTHTjUJpjiwvajRfjcA20h\"}', '2026-04-30 00:54:31'),
(9, 3, 'user', '.', '2026-04-30 00:56:23'),
(10, 3, 'model', 'Hello! I’m PharmaX Assistant, here to help with any questions or user‑management tasks you have. How can I assist you today?', '2026-04-30 00:56:27'),
(11, 12, 'user', 'create a user with id 5', '2026-04-30 09:16:30'),
(12, 12, 'model', '❌ Error: JsonNull', '2026-04-30 09:17:27'),
(13, 3, 'user', 'Create a new user called test, email: test@gmail.com,', '2026-04-30 09:23:25'),
(14, 3, 'model', 'Sure thing! I justneed a couple of details to finish creating the user:\n\n- A password (minimum 6 characters)  \n- The role you’d like the user to have (**Admin** or **Normal User**)\n\nCould you provide those, please?', '2026-04-30 09:23:28'),
(15, 3, 'user', 'Its a normal User with mdp 123456', '2026-04-30 09:23:41'),
(16, 3, 'model', 'Sure! I’ve got theemail (test@gmail.com), role (Normal User), and password (123456).  \nWhat’s the first name for this user?', '2026-04-30 09:23:44'),
(17, 3, 'user', 'Test', '2026-04-30 09:24:06'),
(18, 3, 'model', '✅ User **Test**(test@gmail.com) has been created successfully with the **Normal User** role. Let me know if you need anything else!', '2026-04-30 09:24:10'),
(19, 3, 'user', 'delete user with id 16', '2026-04-30 10:09:54'),
(20, 3, 'model', 'I found a userwith ID 16. Are you sure you want to delete this user?', '2026-04-30 10:09:59'),
(21, 3, 'user', 'yes', '2026-04-30 10:10:51'),
(22, 3, 'model', '✅ User with ID16 (Test) has been successfully deleted.', '2026-04-30 10:10:55'),
(23, 3, 'user', 'whos the user that has id pof 12', '2026-04-30 10:11:28'),
(24, 3, 'model', 'The userwith ID 12 is **Amal Aguir** (email: amal.aguir@esprit.tn), a Normal User who has 2‑FA enabled.', '2026-04-30 10:11:33');

-- --------------------------------------------------------

DROP TABLE IF EXISTS `doctrine_migration_versions`;
--
-- Table structure for table `doctrine_migration_versions`
--

CREATE TABLE `doctrine_migration_versions` (
  `version` varchar(191) NOT NULL,
  `executed_at` datetime DEFAULT NULL,
  `execution_time` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `doctrine_migration_versions`
--

INSERT INTO `doctrine_migration_versions` (`version`, `executed_at`, `execution_time`) VALUES
('DoctrineMigrations\\Version20260212150000', '2026-03-04 22:03:15', 86),
('DoctrineMigrations\\Version20260213000000', '2026-03-04 22:03:15', 57),
('DoctrineMigrations\\Version20260213100000', '2026-03-04 22:03:15', 3),
('DoctrineMigrations\\Version20260225120000', '2026-03-05 14:52:01', 10),
('DoctrineMigrations\\Version20260303150000', '2026-03-04 22:03:15', 3),
('DoctrineMigrations\\Version20260304120000', '2026-03-04 22:03:15', 5),
('DoctrineMigrations\\Version20260304150000', '2026-03-04 22:03:47', 9),
('DoctrineMigrations\\Version20260305134859', '2026-03-05 14:49:51', 1),
('DoctrineMigrations\\Version20260305135019', '2026-03-05 14:50:23', 1);

-- --------------------------------------------------------

DROP TABLE IF EXISTS `messenger_messages`;
--
-- Table structure for table `messenger_messages`
--

CREATE TABLE `messenger_messages` (
  `id` bigint(20) NOT NULL,
  `body` longtext NOT NULL,
  `headers` longtext NOT NULL,
  `queue_name` varchar(190) NOT NULL,
  `created_at` datetime NOT NULL,
  `available_at` datetime NOT NULL,
  `delivered_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

DROP TABLE IF EXISTS `payments`;
--
-- Table structure for table `payments`
--

CREATE TABLE `payments` (
  `id` int(11) NOT NULL,
  `montant` decimal(10,2) NOT NULL,
  `statut` varchar(50) NOT NULL,
  `methode_paiement` varchar(50) NOT NULL,
  `date_paiement` datetime NOT NULL,
  `stripe_session_id` varchar(255) DEFAULT NULL,
  `stripe_payment_intent_id` varchar(255) DEFAULT NULL,
  `stripe_metadata` longtext DEFAULT NULL,
  `transaction_ref` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  `commande_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

DROP TABLE IF EXISTS `reset_password_request`;
--
-- Table structure for table `reset_password_request`
--

CREATE TABLE `reset_password_request` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `selector` varchar(20) NOT NULL,
  `hashed_token` varchar(100) NOT NULL,
  `requested_at` datetime NOT NULL,
  `expires_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

DROP TABLE IF EXISTS `user`;
--
-- Table structure for table `user`
--

CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `email` varchar(180) NOT NULL,
  `roles` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`roles`)),
  `password` varchar(255) NOT NULL,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `status` varchar(16) DEFAULT 'UNBLOCKED',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `google_id` varchar(255) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `google_authenticator_secret` varchar(255) DEFAULT NULL,
  `google_authenticator_secret_pending` varchar(255) DEFAULT NULL,
  `is_2fa_setup_in_progress` tinyint(1) DEFAULT 0,
  `data_face_api` longtext DEFAULT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `face_encoding` text DEFAULT NULL,
  `face_auth_enabled` tinyint(1) DEFAULT 0,
  `failed_attempts` int(11) DEFAULT 0,
  `lockout_time` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`id`, `email`, `roles`, `password`, `first_name`, `last_name`, `status`, `created_at`, `updated_at`, `google_id`, `avatar`, `google_authenticator_secret`, `google_authenticator_secret_pending`, `is_2fa_setup_in_progress`, `data_face_api`, `phone_number`, `face_encoding`, `face_auth_enabled`, `failed_attempts`, `lockout_time`) VALUES
(3, 'lola.aguir@gmail.com', '[\"ROLE_ADMIN\"]', '98b1881548a6e3535da5430f6b1e9f79fae0ea4cb3223cc06616b1fe37357953', 'Amoula', 'User', 'UNBLOCKED', '2026-03-04 22:45:29', '2026-04-16 01:43:32', NULL, 'https://lh3.googleusercontent.com/a/ACg8ocJazZPNkkfBIsQ9eK4PKextVkOheNvweIXIG1FG-xsc5sBx5T8F=s96-c', '5OBZGWO3RASZT3YVT2LMEDY7RWURT64D', NULL, 0, NULL, NULL, NULL, 1, 0, NULL),
(12, 'amal.aguir@esprit.tn', '[\"ROLE_USER\"]', '77990811613dff715a0902b7363d924d545f3ed4c16aa98d05c122beca4885d4', 'amal', 'aguir', 'UNBLOCKED', NULL, NULL, NULL, 'uploads/avatars/user_12_1777540581287.jpg', NULL, NULL, 0, NULL, NULL, '[0.10216523706912994,-1.361247181892395,1.6503984928131104,-0.20093122124671936,-0.11193390935659409,-0.6097149848937988,1.5601089000701904,-1.7197022438049316,-1.1258654594421387,1.3194513320922852,-0.09293895214796066,-0.2543168067932129,0.4027792811393738,0.03643292188644409,-0.37651053071022034,0.35699957609176636,0.4089497923851013,-2.0361292362213135,-0.4867557883262634,-0.5477128624916077,-0.10892701148986816,-0.7728926539421082,-0.24416056275367737,-0.11229323595762253,-0.0994507223367691,0.4694262146949768,2.266545057296753,-1.027443766593933,0.9635133743286133,0.25630441308021545,-0.8620009422302246,-0.1991308182477951,-0.5272743701934814,-1.7147959470748901,1.0027683973312378,0.4425055682659149,-0.49925217032432556,0.4924301207065582,0.7429555654525757,-1.5647063255310059,0.5159643292427063,-0.17014344036579132,-1.018967866897583,1.3866088390350342,-0.699780285358429,-0.40059611201286316,-0.0414840467274189,-0.43195512890815735,-2.1534812450408936,0.08077884465456009,-1.8070144653320312,0.699912428855896,0.04556906968355179,-0.3891940414905548,0.0671967938542366,0.4813438653945923,0.9701740741729736,0.9011164307594299,0.1906999945640564,-0.8181823492050171,-0.7511563897132874,-0.4220355749130249,0.19751524925231934,0.4160255789756775,-1.31833016872406,2.0337460041046143,-0.32362326979637146,0.5540114641189575,0.47907716035842896,1.9251759052276611,-0.2495952546596527,0.1957721710205078,-1.1184604167938232,-0.6491345167160034,0.04253200441598892,0.4325091540813446,-1.2362879514694214,-0.5929744839668274,-0.15813839435577393,-0.9340890049934387,0.2577362060546875,-1.0073744058609009,-0.5234982967376709,0.5870146155357361,2.439997434616089,-1.3212202787399292,0.5152742862701416,-1.09783136844635,-1.1726844310760498,-1.4017812013626099,-0.2996573746204376,-0.7798866033554077,1.2708574533462524,-0.3073461949825287,-0.20382791757583618,0.918881356716156,0.547458291053772,0.10852719843387604,-0.44168928265571594,-1.5340341329574585,-1.3697009086608887,1.4384033679962158,0.2365867644548416,-0.6399215459823608,0.5549342036247253,0.6090186238288879,0.45415008068084717,-0.48826709389686584,0.24720486998558044,0.630794107913971,0.6273805499076843,1.1459721326828003,1.7623505592346191,-0.45335474610328674,-1.5945336818695068,1.3534823656082153,-0.7370686531066895,0.9271816611289978,-1.3232154846191406,0.16966399550437927,1.8779206275939941,0.0881366953253746,1.328861951828003,-0.7953203916549683,0.9658523201942444,-0.19675734639167786,0.64824378490448,0.31810370087623596]', 1, 0, NULL),
(14, 'mouny.moon.gaming@gmail.com', '[\"ROLE_USER\"]', '77990811613dff715a0902b7363d924d545f3ed4c16aa98d05c122beca4885d4', 'amal', 'mouny', 'UNBLOCKED', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, '+21296700337', NULL, 0, 0, NULL),
(15, 'mouny.moon@gmail.com', '[\"ROLE_USER\"]', '77990811613dff715a0902b7363d924d545f3ed4c16aa98d05c122beca4885d4', 'mouny', 'moon', 'UNBLOCKED', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, 0, 5, '2026-04-30 00:35:07');

--

-- --------------------------------------------------------

DROP TABLE IF EXISTS `archive_de_commentaire`;
--
-- Table structure for table `archive_de_commentaire`
--

CREATE TABLE `archive_de_commentaire` (
  `id` int(11) NOT NULL,
  `contenu` longtext NOT NULL,
  `date_publication` datetime DEFAULT NULL,
  `user_name` varchar(100) DEFAULT NULL,
  `user_email` varchar(255) DEFAULT NULL,
  `reason` varchar(255) DEFAULT 'inappropriate',
  `article_id` int(11) DEFAULT NULL,
  `archived_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `archive_de_commentaire`
--

INSERT INTO `archive_de_commentaire` (`id`, `contenu`, `date_publication`, `user_name`, `user_email`, `reason`, `article_id`, `archived_at`) VALUES
(2, 'stupid', '2026-04-30 08:59:34', 'User', NULL, 'inappropriate', 10, '2026-04-30 08:59:34');

-- --------------------------------------------------------

DROP TABLE IF EXISTS `article`;
--
-- Table structure for table `article`
--

CREATE TABLE `article` (
  `id` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `contenu` longtext DEFAULT NULL,
  `contenu_en` longtext DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `likes` int(11) DEFAULT 0,
  `is_draft` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `article`
--

INSERT INTO `article` (`id`, `titre`, `contenu`, `contenu_en`, `image`, `created_at`, `updated_at`, `likes`, `is_draft`) VALUES
(1, 'Les bienfaits de la vitamine D en hiver', 'La vitamine D joue un rôle crucial dans le maintien de la santé osseuse et du système immunitaire. Pendant les mois d\'hiver, notre exposition au soleil diminue considérablement, ce qui peut entraîner une carence en vitamine D. Il est recommandé de consommer des aliments riches en vitamine D comme les poissons gras, les œufs et les produits laitiers enrichis. Un complément alimentaire peut également être envisagé après consultation avec votre pharmacien.', NULL, 'C:\\Users\\Asus\\Documents\\pharmax\\public\\images\\1200x680_sc_000-36t99m7.jpg', '2026-04-28 19:11:15', '2026-04-29 20:14:50', 0, 0),
(2, 'Comment bien gérer son traitement antibiotique22', 'Les antibiotiques sont des médicaments puissants qui combattent les infections bactériennes. Il est essentiel de respecter la durée du traitement prescrit, même si les symptômes s\'améliorent. Prendre un antibiotique de manière incorrecte peut contribuer à la résistance bactérienne. Votre pharmacien PharmaX est là pour vous conseiller sur la prise optimale de vos médicaments.', NULL, 'C:\\Users\\Asus\\Documents\\pharmax\\public\\images\\75480feec87fe60a14a866798b78510b4d363996.jpg', '2026-04-28 19:11:15', '2026-04-29 20:14:50', 0, 0),
(3, 'Prévention de la grippe saisonnière : nos conseils', 'La saison de la grippe approche et il est important de se préparer. La vaccination reste le moyen le plus efficace de se protéger. En complément, adoptez les gestes barrières : lavage fréquent des mains, port du masque dans les lieux publics, et aération régulière des espaces clos. Rendez-vous dans votre pharmacie PharmaX pour votre vaccin antigrippal.', NULL, 'C:\\Users\\Asus\\Documents\\pharmax\\public\\images\\ACTUS2.webp', '2026-04-28 19:11:15', '2026-04-29 20:14:50', 0, 0),
(6, 'Title: Vaccins : une clé pour protéger la santé', 'Introduction\n\nLes vaccins sont un outil crucial dans la lutte contre les maladies infectieuses. Ils ont permis de protéger des millions de personnes et d\'éliminer certaines maladies mortelles, tels que la variole et la rougeole. Aujourd\'hui, la vaccination reste un sujet important pour préserver la santé publique.\n\nSection 1 : Qu\'est-ce qu\'un vaccin ?\n\nUn vaccin est une substance qui stimule le système immunitaire pour que votre organisme puisse lutter contre une maladie particulière sans que vous ne contractiez réellement la maladie. Les vaccins sont composés de parties ou de tout ou partie d\'agents pathogènes, comme des virus ou des bactéries, qui ont été modifiés pour être moins dangereux. Ils peuvent également contenir des protéines ou des toxines produites par les agents pathogènes.\n\nSection 2 : Pourquoi les vaccins sont-ils importants ?\n\nLes vaccins permettent de prévenir la transmission des maladies infectieuses et d\'éviter leur propagation. Ils protègent non seulement le', NULL, 'C:\\Users\\Asus\\Documents\\pharmax\\public\\images\\1683964958_systeme-de-sante-en-tunisie.jpg', '2026-04-28 22:43:27', '2026-04-28 22:43:27', 0, 0),
(7, 'Le Cancer : Un Ennemi à Battre en Tous Les Cas', 'Introduction\n\nLe cancer est l\'une des principales causes de décès dans le monde occidental. Malheureusement, il touche nombre de personnes chaque année. Cependant, grâce aux progrès de la recherche médicale, les traitements et les soins palliatifs pour le cancer ont grandement évolué ces dernières années.\n\nSection 1 : Qu\'est-ce que le Cancer?\n\nLe cancer est une maladie caractérisée par l\'apparition de cellules cancéreuses, qui multiplient et se propagent rapidement dans le corps. Ces cellules ne sont pas contrôlées normalement et peuvent former des tumeurs. Le cancer peut toucher n\'importe quel organe du corps humain.\n\nSection 2 : Les Traitements du Cancer\n\nIl existe plusieurs types de traitements pour le cancer, dont la chimiothérapie, la radiothérapie et la chirurgie. La chimiothérapie consiste à administrer des médicaments qui tuent les cellules cancéreuses en les empêchant de se reproduire correctement. La radiothérapie utilise des rayons X pour détruire les cellules', 'Introduction\n\nCancer is one of the major causes of death in the Western world. Unfortunately, it affects many people each year. However, thanks to advancements in medical research, treatments and palliative care for cancer have significantly evolved over the past few years.\n\nSection 1 : What is Cancer?\n\nCancer is a disease characterized by the appearance of cancerous cells that multiply and spread rapidly throughout the body. These cells are not controlled normally and can form tumors. Cancer can affect any organ in the human body.\n\nSection 2 : Treatments for Cancer\n\nThere are several types of treatments for cancer, including chemotherapy, radiotherapy, and surgery. Chemotherapy involves administering medications that kill cancerous cells by preventing them from reproducing correctly. Radiotherapy uses X-rays to destroy cancerous cells.', 'C:\\Users\\Asus\\Documents\\pharmax\\public\\images\\wp2655122.jpg', '2026-04-29 19:14:25', '2026-04-29 19:31:19', 0, 0),
(8, 'Santé en Tunisie : Un regard sur le système de soins de santé', 'Introduction\n\nLa Tunisie dispose d\'un système de soins de santé solide et bien structuré, qui a connu une évolution constante au fil des années. Ce système est composé d\'une multitude d\'acteurs tels que les hôpitaux publics, privés et universitaires ainsi que des centres médicaux de première ligne.\n\nSanté primaire\n\nLa santé primaire constitue la base du système de soins de santé en Tunisie. Elle est assurée par un réseau d\'établissements de santé publique, qui offrent des services de consultation médicale, vaccination et diagnostic aux patients. Les centres de santé primaire sont distribués à travers tout le pays et ont pour objectif de permettre un accès équitable aux soins de base pour toutes les populations.\n\nSanté secondaire et tertiaire\n\nLa santé secondaire et tertiaire en Tunisie se déroule dans des hôpitaux publics, privés et universitaires. Les hôpitaux publics offrent des soins de qualité à un prix abordable pour la', NULL, 'C:\\Users\\Asus\\Documents\\pharmax\\public\\images\\1683964958_systeme-de-sante-en-tunisie.jpg', '2026-04-29 20:38:09', '2026-04-29 21:52:20', 0, 0),
(9, 'Title: L\'État de la Santé Mentale en Tunisie : Un Défi Croissant', 'Introduction\n\nLa Tunisie a connu une évolution significative dans le domaine de la santé mentale ces dernières années. Malgré des progrès notables, le pays continue d\'affronter plusieurs défis pour améliorer la prise en charge et l\'accessibilité aux soins psychologiques.\n\nSection 1: Les Progrès Récemment Accomplis\n\nLes initiatives gouvernementales ont permis de mettre en place des structures de santé mentale plus performantes, telles que les hôpitaux psychiatriques et les centres de soins communautaires. Des programmes d\'éducation pour la santé mentale ont également été mis en place dans les écoles et les universités, ce qui a permis de sensibiliser une large population à l\'importance de la santé mentale.\n\nSection 2: Les Défis Actuels et les Solutions Potentielles\n\nMalgré ces progrès, la Tunisie continue d\'affronter des défis importants dans le domaine de la santé mentale. La stigmatisation persiste, empêchant nombre de personnes atteintes de troubles mentaux', NULL, 'C:\\Users\\Asus\\Documents\\pharmax\\public\\images\\75480feec87fe60a14a866798b78510b4d363996.jpg', '2026-04-29 20:57:26', '2026-04-29 21:52:20', 0, 0),
(10, 'Les Vitamines : Clés pour une Santé Optimale', 'Introduction:\nLes vitamines sont des nutriments essentiels pour la vie humaine, nécessaires à l\'équilibre et à la fonctionnement normal de tous nos organes et systèmes. Elles interviennent dans de nombreux processus physiologiques, tels que la croissance, la réparation de tissus, la production d\'énergie, le métabolisme des protéines, les fonctions immunitaires et la santé oculaire.\n\nSection 1: Les Vitamines Essentielles\nIl existe douze vitamines essentielles pour l\'être humain, classées en deux groupes : les vitamines liposolubles (A, D, E, K) et les vitamines hydrosolubles (B et C).\n\nLes vitamines liposolubles sont stockées dans le foie et l\'adipose pour être utilisées à long terme. Elles peuvent également être stockées dans certains tissus tels que les os, la peau et les cheveux. La vitamine A (rétinol) est importante pour la vision nocturne, la croissance et le développement des os, tandis que la vitamine D aide à absor', NULL, 'C:\\Users\\Asus\\Documents\\pharmax\\public\\images\\png-transparent-dietary-supplement-vitamin-d-capsule-tablet-tablet-electronics-nutrition-dietary-supplement.png', '2026-04-29 21:04:06', '2026-04-29 21:52:20', 0, 0),
(11, 'Statistiques sur la Covid-19 en Tunisie : une situation en évolution', 'Introduction\n\nLa Tunisie a connu son premier cas de COVID-19 le 2 mars 2020. Depuis cette date, le pays a continué à suivre les évolutions de la pandémie et à mettre en place des mesures sanitaires pour limiter sa propagation. Voici une synthèse des statistiques actuelles sur la Covid-19 en Tunisie.\n\nSection 1 : Les cas confirmés et les décès\n\nSelon les données de l\'Institut national de la santé publique (INSP), le nombre total de cas confirmés de COVID-19 en Tunisie a dépassé les 240 000 cas fin décembre 2020. Le nombre de décès liés à la maladie est également monté, avec plus de 7 500 morts enregistrés depuis le début de la pandémie.\n\nSection 2 : Les taux de guérison et les cas actifs\n\nBien que le nombre total de cas confirmés soit élevé, la majorité des patients ont récupéré de la maladie. Selon les données de l\'INSP, le taux de guér', NULL, 'C:\\Users\\Asus\\Documents\\pharmax\\public\\images\\506449242_1131296195707508_8653262443213233050_n_0_0.jpg', '2026-04-29 21:08:45', '2026-04-29 21:52:20', 1, 0),
(12, 'La BB (Bulletproof Coffee) : Une boisson à la mode dans le monde de l\'alimentation santé en France', 'Introduction\n\nLa BB, ou Bulletproof Coffee, est une boisson populaire aux États-Unis qui a commencé à gagner en popularité en France. Originaire du mouvement de vie au style kétogénique, la BB est un café préparé avec du beurre gras de vache et de l\'huile de noisette, plutôt que du lait ou de la crème.\n\nSection 1 : Les bienfaits supposés de la BB\n\nLa BB est réputée pour être une boisson riche en énergie, qui peut aider à satiérer les appétits et à maintenir une alimentation kétogénique. De plus, le beurre gras de vache contient des acides gras saturés, qui sont considérés comme positifs pour la santé cardiovasculaire en raison de leur capacité à augmenter les niveaux de cholestérol HDL (le cholestérol bon). De plus, l\'huile de noisette contient des acides gras omega-3 bénéfiques pour la santé.\n\nSection 2 : Les', NULL, 'C:\\Users\\Asus\\Documents\\pharmax\\public\\images\\136920.jpg', '2026-04-29 21:20:25', '2026-05-04 23:45:49', 3, 0),
(13, 'Pillules : Qu\'est-ce que cela veut dire et quelles sont leurs avantages pour la santé ?', 'Introduction:\nLes pillules sont un type de médicaments en forme de petites capsules ou tablettes qui peuvent être prises oralement. Elles sont largement utilisées dans le traitement de diverses maladies et conditions de santé, ainsi que pour prévenir certaines maladies.\n\nSection 1 : Les pillules et leur rôle dans la santé\nLes pillules peuvent contenir des médicaments actifs qui agissent sur les organes internes ou les systèmes du corps humain. Elles sont prescrites par des médecins pour traiter diverses affections, telles que l\'hypertension artérielle, la maladie d\'Alzheimer, les infections et inflammations, ainsi que pour le contrôle de la naissance ou la régulation des hormones du corps.\n\nLes pillules peuvent également être utilisées pour prévenir certaines maladies, comme les pilules anti-inflammatoires non stéroïdiens (AINS) pour prévenir les maladies cardiovasculaires et les pilules contraceptives pour éviter une grossesse indésirable.\n\nSection 2 : Les précaut', NULL, 'C:\\Users\\Asus\\Documents\\pharmax\\public\\images\\image.jpg', '2026-04-29 21:24:41', '2026-04-29 21:52:20', 1, 0),
(14, 'Titre : Les Vitamines : Clés de l\'Énergie et de la Santé', 'Intro :\nLes vitamines sont des molécules essentielles pour le bon fonctionnement de notre corps humain. Elles interviennent dans la croissance, le développement, la réparation, l\'immunité et la régulation métabolique. Malgré leur petite taille, les vitamines ont un impact majeur sur la santé en apportant des nutriments indispensables à notre organisme.\n\nSection 1 : Les Vitamines essentielles pour l\'Homme\nIl existe douze vitamines essentielles pour l\'homme, classées en deux groupes : les vitamines liposolubles (A, D, E et K) et les vitamines hydrosolubles (B1, B2, B3, B5, B6, B7, B9 et B12).\n\nLes vitamines liposolubles sont stockées dans le foie et les graisses du corps. Elles interviennent dans la formation des cellules, l\'absorption des nutriments, la croissance osseuse et la coagulation du sang. La Vitamine A est indispensable pour la vision nocturn', NULL, 'C:\\Users\\Asus\\Documents\\pharmax\\public\\images\\1-Medical-Software-Development-Business-Transforming-the-Healthcare-Industry.jpg', '2026-04-30 08:37:01', '2026-04-30 08:57:44', 0, 1),
(16, 'Rosacée : Qu\'est-ce que cela veut dire pour ma santé ?', 'Introduction\n\nLa rosacée est une condition cutanée courante qui affecte principalement la peau du visage. Elle se caractérise par des éruptions rouges et enflées, ainsi qu\'une apparence de rougeur permanente. Bien que ce ne soit pas une maladie grave, la rosacée peut être embarrassante pour certains patients et nécessiter un traitement approprié.\n\nSection 1 : Les causes de la rosacée\n\nIl existe plusieurs facteurs qui peuvent contribuer à la rosacée. La plupart des cas sont causés par une maladie du système immunitaire appelée télinguectasie faciale héréditaires. D\'autres causes peuvent inclure l\'exposition excessive au soleil, le stress, les alcools et les aliments qui favorisent la rougeur de peau. Les personnes ayant un teint clair sont plus susceptibles d\'être touchées par cette condition.\n\nSection 2 : Le traitement de la rosacée\n\nIl existe plusieurs options de traitement pour la rosacée, qui varient en fonction de la gravité de la condition et du patient. Les méd', NULL, 'C:\\Users\\Asus\\Documents\\pharmax\\public\\images\\1683964958_systeme-de-sante-en-tunisie.jpg', '2026-04-30 08:56:53', '2026-04-30 10:01:03', 1, 0);

-- --------------------------------------------------------

DROP TABLE IF EXISTS `categorie`;
--
-- Table structure for table `categorie`
--

CREATE TABLE `categorie` (
  `id` int(11) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `categorie`
--

INSERT INTO `categorie` (`id`, `nom`, `description`, `created_at`) VALUES
(1, 'Medicaments', 'Medicaments sur ordonnance et en vente libre', '2026-05-04 23:07:41'),
(2, 'Vitamines', 'Complements alimentaires et vitamines', '2026-05-04 23:07:41'),
(3, 'Hygiene', 'Produits de soin et hygiene', '2026-05-04 23:07:41'),
(4, 'Materiel Medical', 'Equipements et materiel medical', '2026-05-04 23:07:41');

-- --------------------------------------------------------

DROP TABLE IF EXISTS `commandes`;
--
-- Table structure for table `commandes`
--

CREATE TABLE `commandes` (
  `id` int(11) NOT NULL,
  `produits` text DEFAULT NULL,
  `totales` double DEFAULT NULL,
  `statut` varchar(50) DEFAULT NULL,
  `created_date` datetime NOT NULL DEFAULT current_timestamp(),
  `utilisateur_id` int(11) DEFAULT NULL,
  `qr_token` varchar(64) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `commandes`
--

INSERT INTO `commandes` (`id`, `produits`, `totales`, `statut`, `created_date`, `utilisateur_id`, `qr_token`) VALUES
(1, '[{\"nom\":\"Produit 1\",\"quantite\":4,\"prix\":10.00,\"sousTotal\":40.00},{\"nom\":\"Produit 2\",\"quantite\":1,\"prix\":20.00,\"sousTotal\":20.00}]', 71.4, 'En attente', '2026-05-04 22:37:44', 3, 'W5G9MHYFZ6LNSJ5ASB'),
(2, '[{\"nom\":\"Multivitamines\",\"quantite\":1,\"prix\":7.99,\"sousTotal\":7.99},{\"nom\":\"Vitamine C 500mg\",\"quantite\":1,\"prix\":6.99,\"sousTotal\":6.99}]', 10.8262, 'En attente', '2026-05-04 23:39:53', 3, 'ERR952EFE5N955E8NH'),
(3, '[{\"nom\":\"Vitamine C 500mg\",\"quantite\":1,\"prix\":6.99,\"sousTotal\":6.99},{\"nom\":\"Multivitamines\",\"quantite\":1,\"prix\":7.99,\"sousTotal\":7.99}]', 16.8262, 'En attente', '2026-05-04 23:57:42', 3, 'XBFTXG9C7ZRFWNFCMP'),
(4, '[{\"nom\":\"Thermometre numerique\",\"quantite\":1,\"prix\":13.99,\"sousTotal\":13.99}]', 31.2481, 'En attente', '2026-05-05 12:01:28', 3, 'FCJUCXG849BK9FM3DE'),
(5, '[{\"nom\":\"Aspirine 500mg\",\"quantite\":1,\"prix\":3.99,\"sousTotal\":3.99},{\"nom\":\"Paracetamol 500mg\",\"quantite\":1,\"prix\":3.49,\"sousTotal\":3.49}]', 21.50358, 'En attente', '2026-05-05 17:20:11', 3, 'QFPQQZURV8GS4TXPQE');

-- --------------------------------------------------------

DROP TABLE IF EXISTS `commentaire`;
--
-- Table structure for table `commentaire`
--

CREATE TABLE `commentaire` (
  `id` int(11) NOT NULL,
  `contenu` longtext NOT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `statut` varchar(20) DEFAULT 'en_attente',
  `user_name` varchar(100) DEFAULT NULL,
  `article_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `commentaire`
--

INSERT INTO `commentaire` (`id`, `contenu`, `created_at`, `statut`, `user_name`, `article_id`) VALUES
(1, 'Merci pour ces explications claires !', '2026-04-28 19:11:15', 'valide', NULL, 1),
(2, 'Article très informatif et utile.', '2026-04-28 19:11:15', 'valide', NULL, 1),
(3, 'Information importante sur la résistance bactérienne.', '2026-04-28 19:11:15', 'valide', NULL, 2),
(4, 'Merci pour ces conseils pratiques.', '2026-04-28 19:11:15', 'valide', NULL, 3);

-- --------------------------------------------------------

DROP TABLE IF EXISTS `lignes_commandes`;
--
-- Table structure for table `lignes_commandes`
--

CREATE TABLE `lignes_commandes` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) DEFAULT NULL,
  `prix` double DEFAULT NULL,
  `quantite` int(11) DEFAULT NULL,
  `sous_total` double DEFAULT NULL,
  `commande_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `lignes_commandes`
--

INSERT INTO `lignes_commandes` (`id`, `nom`, `prix`, `quantite`, `sous_total`, `commande_id`) VALUES
(1, 'Produit 1', 10, 4, 40, 1),
(2, 'Produit 2', 20, 1, 20, 1),
(3, 'Multivitamines', 7.99, 1, 7.99, 2),
(4, 'Vitamine C 500mg', 6.99, 1, 6.99, 2),
(5, 'Vitamine C 500mg', 6.99, 1, 6.99, 3),
(6, 'Multivitamines', 7.99, 1, 7.99, 3),
(7, 'Thermometre numerique', 13.99, 1, 13.99, 4),
(8, 'Aspirine 500mg', 3.9920000000000004, 1, 3.9920000000000004, 5),
(9, 'Paracetamol 500mg', 3.49, 1, 3.49, 5);

-- --------------------------------------------------------

DROP TABLE IF EXISTS `livraisons`;
--
-- Table structure for table `livraisons`
--

CREATE TABLE `livraisons` (
  `id` int(11) NOT NULL,
  `last_name` varchar(100) DEFAULT NULL,
  `first_name` varchar(100) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `adresse` varchar(255) DEFAULT NULL,
  `tel` varchar(30) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `commande_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `livraisons`
--

INSERT INTO `livraisons` (`id`, `last_name`, `first_name`, `email`, `adresse`, `tel`, `created_at`, `commande_id`) VALUES
(1, 'nayrouzdaikhi', 'nayrouzdaikhi', 'nayrouzdaikhi@gmail.com', 'ariana', '29283918', '2026-05-04 22:37:44', 1),
(2, 'nayrouzdaikhi', 'nayrouzdaikhi', 'nayrouzdaikhi@gmail.com', 'ariana', '29283918', '2026-05-04 23:39:53', 2),
(3, 'nayrouzdaikhi', 'nayrouzdaikhi', 'nayrouzdaikhi@gmail.com', 'ariana', '29283918', '2026-05-04 23:57:42', 3),
(4, 'nayrouzdaikhi', 'nayrouzdaikhi', 'nayrouzdaikhi@gmail.com', 'manouba', '29283918', '2026-05-05 12:01:28', 4),
(5, 'nayrouzdaikhi', 'nayrouzdaikhi', 'nayrouzdaikhi@gmail.com', 'manouba', '29283918', '2026-05-05 17:20:11', 5);

-- --------------------------------------------------------

DROP TABLE IF EXISTS `notifications`;
--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `message` text DEFAULT NULL,
  `type` varchar(50) DEFAULT 'info',
  `is_read` tinyint(1) DEFAULT 0,
  `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `notifications`
--

INSERT INTO `notifications` (`id`, `user_id`, `title`, `message`, `type`, `is_read`, `created_at`) VALUES
(1, 1, 'Commande Validée 🛒', 'Votre commande #2 a bien été enregistrée. Merci pour votre confiance !', 'ORDER_VALIDATED', 1, '2026-05-04 23:39:53'),
(2, 1, 'Commande Validée 🛒', 'Votre commande #3 a bien été enregistrée. Merci pour votre confiance !', 'ORDER_VALIDATED', 1, '2026-05-04 23:57:42'),
(3, 1, 'Commande Validée 🛒', 'Votre commande #4 a bien été enregistrée. Merci pour votre confiance !', 'ORDER_VALIDATED', 1, '2026-05-05 12:01:28'),
(4, 1, 'Commande Validée 🛒', 'Votre commande #5 a bien été enregistrée. Merci pour votre confiance !', 'ORDER_VALIDATED', 1, '2026-05-05 17:20:11');

-- --------------------------------------------------------

DROP TABLE IF EXISTS `notification_state`;
--
-- Table structure for table `notification_state`
--

CREATE TABLE `notification_state` (
  `id` int(11) NOT NULL,
  `produit_id` int(11) DEFAULT NULL,
  `is_read` tinyint(1) DEFAULT 0,
  `is_dismissed` tinyint(1) DEFAULT 0,
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

DROP TABLE IF EXISTS `produit`;
--
-- Table structure for table `produit`
--

CREATE TABLE `produit` (
  `id` int(11) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `prix` double NOT NULL DEFAULT 0,
  `prix_final` double DEFAULT 0,
  `image` varchar(500) DEFAULT NULL,
  `date_expiration` date DEFAULT NULL,
  `statut` varchar(50) DEFAULT 'actif',
  `created_at` datetime DEFAULT current_timestamp(),
  `quantite` int(11) DEFAULT 0,
  `categorie_id` int(11) DEFAULT NULL,
  `promo_code` varchar(50) DEFAULT NULL,
  `discount_percentage` double DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `produit`
--

INSERT INTO `produit` (`id`, `nom`, `description`, `prix`, `prix_final`, `image`, `date_expiration`, `statut`, `created_at`, `quantite`, `categorie_id`, `promo_code`, `discount_percentage`) VALUES
(1, 'Aspirine 500mg', 'Analgesique et anti-inflammatoire - 20 comprimes', 4.99, 3.9920000000000004, 'C:\\Users\\Asus\\Documents\\pharmax\\public\\images\\D1200012663457321_171120230301.jpg', '2026-05-05', 'actif', '2026-05-04 23:07:41', 50, 1, 'EXPIRE20-DXG2', 20),
(2, 'Paracetamol 500mg', 'Anti-douleur et anti-fievre - 16 comprimes', 3.49, 3.49, NULL, NULL, 'actif', '2026-05-04 23:07:41', 75, 1, NULL, 0),
(3, 'Ibuprofene 200mg', 'Anti-inflammatoire non steroidien - 24 comprimes', 5.99, 4.79, NULL, NULL, 'actif', '2026-05-04 23:07:41', 60, 1, NULL, 20),
(4, 'Vitamine C 500mg', 'Vitamine C - 30 comprimes', 6.99, 6.99, NULL, NULL, 'actif', '2026-05-04 23:07:41', 80, 2, NULL, 0),
(5, 'Multivitamines', 'Complement multivitamine - 30 comprimes', 9.99, 7.99, NULL, NULL, 'actif', '2026-05-04 23:07:41', 45, 2, NULL, 20),
(6, 'Gel antibacterien', 'Desinfectant pour mains - 100ml', 3.99, 3.99, NULL, NULL, 'actif', '2026-05-04 23:07:41', 120, 3, NULL, 0),
(7, 'Thermometre numerique', 'Thermometre sans contact', 15.99, 13.99, NULL, NULL, 'actif', '2026-05-04 23:07:41', 20, 4, NULL, 12.5),
(8, 'Masques chirurgicaux', 'Boite de 50 masques', 6.99, 6.99, NULL, NULL, 'actif', '2026-05-04 23:07:41', 75, 3, NULL, 0);

-- --------------------------------------------------------

DROP TABLE IF EXISTS `produits`;
--
-- Table structure for table `produits`
--

CREATE TABLE `produits` (
  `id` int(11) NOT NULL,
  `nom` varchar(255) DEFAULT NULL,
  `prix` double DEFAULT NULL,
  `description` text DEFAULT NULL,
  `stock` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `produits`
--

INSERT INTO `produits` (`id`, `nom`, `prix`, `description`, `stock`) VALUES
(1, 'Aspirine 500mg', 4.99, 'Analgésique - 20 comprimés', 50),
(2, 'Paracétamol 500mg', 3.49, 'Anti-douleur - 16 comprimés', 75),
(3, 'Ibuprofène 200mg', 5.99, 'Anti-inflammatoire - 24 comprimés', 60),
(4, 'Sirop contre la toux', 8.5, 'Expectorant - 200ml', 30),
(5, 'Spray nasal', 6.99, 'Décongestionnant - 15ml', 45),
(6, 'Pastilles gorge', 2.99, 'Menthe - 20 pastilles', 100),
(7, 'Oméprazole 20mg', 7.5, 'Anti-reflux - 14 gélules', 40),
(8, 'Charbon actif', 5.49, 'Troubles digestifs - 30 comprimés', 55),
(9, 'Probiotiques', 12.99, 'Flore intestinale - 30 gélules', 25),
(10, 'Vitamines C', 6.99, 'Vitamine C - 30 comprimés', 80),
(11, 'Multivitamines', 9.99, 'Complément - 30 comprimés', 45),
(12, 'Calcium + D', 10.99, 'Santé osseuse - 30 comprimés', 35),
(13, 'Magnésium', 8.49, 'Relaxation - 60 gélules', 50),
(14, 'Antihistaminique', 7.99, 'Anti-allergène - 30 comprimés', 65),
(15, 'Pommade anti-itch', 5.99, 'Crème - 50g', 40),
(16, 'Gel antibactérien', 3.99, 'Désinfectant - 100ml', 120),
(17, 'Pansements', 4.99, '30 pansements', 90),
(18, 'Thermomètre', 15.99, 'Sans contact - 1 unité', 20),
(19, 'Masques', 6.99, '50 masques', 75),
(20, 'Écran solaire', 11.99, 'SPF 50 - 200ml', 55),
(21, 'Crème hydratante', 9.49, 'Soin peau - 100ml', 40),
(22, 'Shampooing', 7.99, 'Médical - 250ml', 35),
(23, 'Mélatonine', 8.99, 'Sommeil - 60 comprimés', 50),
(24, 'Glucosamine', 14.99, 'Articulations - 60 gélules', 25),
(25, 'Zinc', 6.49, 'Immunité - 30 comprimés', 70);

-- --------------------------------------------------------

DROP TABLE IF EXISTS `reclamation`;
--
-- Table structure for table `reclamation`
--

CREATE TABLE `reclamation` (
  `id` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `date_creation` date DEFAULT curdate(),
  `statut` varchar(50) DEFAULT 'en attente',
  `user_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `reclamation`
--

INSERT INTO `reclamation` (`id`, `titre`, `description`, `date_creation`, `statut`, `user_id`) VALUES
(1, 'service tres lent', 'votre service est tres lents et vous ne repndez pas rapidement', '2026-05-04', 'RÉSOLUE', NULL);

-- --------------------------------------------------------

DROP TABLE IF EXISTS `reponse`;
--
-- Table structure for table `reponse`
--

CREATE TABLE `reponse` (
  `id` int(11) NOT NULL,
  `contenu` text NOT NULL,
  `date_reponse` date DEFAULT curdate(),
  `reclamation_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `reponse`
--

INSERT INTO `reponse` (`id`, `contenu`, `date_reponse`, `reclamation_id`) VALUES
(1, 'Je suis désolé d\'entendre que vous avez des retards dans nos réponses. Nous travaillons actuellement pour améliorer notre temps de réponse et nous vous remercions de votre patience. Je peux essayer de vous aider immédiatement ou vous pouvez nous contacter demain pour une réponse plus rapide.', '2026-05-04', 1);

--
-- Indexes for dumped tables
--

-- Indexes for table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idx_email` (`email`),
  ADD KEY `idx_status` (`status`);

-- Indexes for table `admin_log`
--
ALTER TABLE `admin_log`
  ADD PRIMARY KEY (`id`),
  ADD KEY `admin_id` (`admin_id`);

-- Indexes for table `chat_message`
--
ALTER TABLE `chat_message`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

-- Indexes for table `reset_password_request`
--
ALTER TABLE `reset_password_request`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_7CE748A1E7927C74` (`user_id`),
  ADD KEY `idx_selector` (`selector`);

-- Indexes for table `doctrine_migration_versions`
--
ALTER TABLE `doctrine_migration_versions`
  ADD PRIMARY KEY (`version`);

-- Indexes for table `payments`
--
ALTER TABLE `payments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_payment_commande` (`commande_id`),
  ADD KEY `idx_stripe_session` (`stripe_session_id`);

-- Indexes for table `messenger_messages`
--
ALTER TABLE `messenger_messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_75EA56E0FB7336F0E3BD61CE16BA31DBBF396750` (`queue_name`,`available_at`,`delivered_at`,`id`);

-- Indexes for table `archive_de_commentaire`
--
ALTER TABLE `archive_de_commentaire`
  ADD PRIMARY KEY (`id`),
  ADD KEY `article_id` (`article_id`);

-- Indexes for table `article`
--
ALTER TABLE `article`
  ADD PRIMARY KEY (`id`);

-- Indexes for table `categorie`
--
ALTER TABLE `categorie`
  ADD PRIMARY KEY (`id`);

-- Indexes for table `commandes`
--
ALTER TABLE `commandes`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_commandes_qr_token` (`qr_token`);

-- Indexes for table `commentaire`
--
ALTER TABLE `commentaire`
  ADD PRIMARY KEY (`id`),
  ADD KEY `article_id` (`article_id`);

-- Indexes for table `lignes_commandes`
--
ALTER TABLE `lignes_commandes`
  ADD PRIMARY KEY (`id`),
  ADD KEY `commande_id` (`commande_id`);

-- Indexes for table `livraisons`
--
ALTER TABLE `livraisons`
  ADD PRIMARY KEY (`id`),
  ADD KEY `commande_id` (`commande_id`);

-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

-- Indexes for table `notification_state`
--
ALTER TABLE `notification_state`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `produit_id` (`produit_id`);

-- Indexes for table `produit`
--
ALTER TABLE `produit`
  ADD PRIMARY KEY (`id`),
  ADD KEY `categorie_id` (`categorie_id`);

-- Indexes for table `produits`
--
ALTER TABLE `produits`
  ADD PRIMARY KEY (`id`);

-- Indexes for table `reclamation`
--
ALTER TABLE `reclamation`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

-- Indexes for table `reponse`
--
ALTER TABLE `reponse`
  ADD PRIMARY KEY (`id`),
  ADD KEY `reclamation_id` (`reclamation_id`);

--
-- AUTO_INCREMENT for dumped tables
--

-- AUTO_INCREMENT for table `user`
--
ALTER TABLE `user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

-- AUTO_INCREMENT for table `admin_log`
--
ALTER TABLE `admin_log`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

-- AUTO_INCREMENT for table `chat_message`
--
ALTER TABLE `chat_message`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=25;

-- AUTO_INCREMENT for table `reset_password_request`
--
ALTER TABLE `reset_password_request`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

-- AUTO_INCREMENT for table `payments`
--
ALTER TABLE `payments`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

-- AUTO_INCREMENT for table `messenger_messages`
--
ALTER TABLE `messenger_messages`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

-- AUTO_INCREMENT for table `archive_de_commentaire`
--
ALTER TABLE `archive_de_commentaire`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

-- AUTO_INCREMENT for table `article`
--
ALTER TABLE `article`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

-- AUTO_INCREMENT for table `categorie`
--
ALTER TABLE `categorie`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

-- AUTO_INCREMENT for table `commandes`
--
ALTER TABLE `commandes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

-- AUTO_INCREMENT for table `commentaire`
--
ALTER TABLE `commentaire`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

-- AUTO_INCREMENT for table `lignes_commandes`
--
ALTER TABLE `lignes_commandes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

-- AUTO_INCREMENT for table `livraisons`
--
ALTER TABLE `livraisons`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

-- AUTO_INCREMENT for table `notification_state`
--
ALTER TABLE `notification_state`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

-- AUTO_INCREMENT for table `produit`
--
ALTER TABLE `produit`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

-- AUTO_INCREMENT for table `produits`
--
ALTER TABLE `produits`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=26;

-- AUTO_INCREMENT for table `reclamation`
--
ALTER TABLE `reclamation`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

-- AUTO_INCREMENT for table `reponse`
--
ALTER TABLE `reponse`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- Constraints for dumped tables
--

-- Constraints for table `admin_log`
--
ALTER TABLE `admin_log`
  ADD CONSTRAINT `admin_log_ibfk_1` FOREIGN KEY (`admin_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

-- Constraints for table `chat_message`
--
ALTER TABLE `chat_message`
  ADD CONSTRAINT `chat_message_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

-- Constraints for table `reset_password_request`
--
ALTER TABLE `reset_password_request`
  ADD CONSTRAINT `reset_password_request_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

-- Constraints for table `payments`
--
ALTER TABLE `payments`
  ADD CONSTRAINT `FK_65D29B3282EA2E54` FOREIGN KEY (`commande_id`) REFERENCES `commandes` (`id`) ON DELETE CASCADE;

-- Constraints for table `archive_de_commentaire`
--
ALTER TABLE `archive_de_commentaire`
  ADD CONSTRAINT `archive_de_commentaire_ibfk_1` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`) ON DELETE SET NULL;

-- Constraints for table `commentaire`
--
ALTER TABLE `commentaire`
  ADD CONSTRAINT `commentaire_ibfk_1` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`) ON DELETE CASCADE;

-- Constraints for table `lignes_commandes`
--
ALTER TABLE `lignes_commandes`
  ADD CONSTRAINT `lignes_commandes_ibfk_1` FOREIGN KEY (`commande_id`) REFERENCES `commandes` (`id`) ON DELETE CASCADE;

-- Constraints for table `livraisons`
--
ALTER TABLE `livraisons`
  ADD CONSTRAINT `livraisons_ibfk_1` FOREIGN KEY (`commande_id`) REFERENCES `commandes` (`id`) ON DELETE CASCADE;

-- Constraints for table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

-- Constraints for table `notification_state`
--
ALTER TABLE `notification_state`
  ADD CONSTRAINT `notification_state_ibfk_1` FOREIGN KEY (`produit_id`) REFERENCES `produit` (`id`) ON DELETE CASCADE;

-- Constraints for table `produit`
--
ALTER TABLE `produit`
  ADD CONSTRAINT `produit_ibfk_1` FOREIGN KEY (`categorie_id`) REFERENCES `categorie` (`id`) ON DELETE SET NULL;

-- Constraints for table `reclamation`
--
ALTER TABLE `reclamation`
  ADD CONSTRAINT `reclamation_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL;

-- Constraints for table `reponse`
--
ALTER TABLE `reponse`
  ADD CONSTRAINT `reponse_ibfk_1` FOREIGN KEY (`reclamation_id`) REFERENCES `reclamation` (`id`) ON DELETE CASCADE;
SET FOREIGN_KEY_CHECKS = 1;
COMMIT;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
