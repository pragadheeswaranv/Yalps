package com.example.data

import com.example.model.DailyMix
import com.example.model.LyricLine
import com.example.model.Song
import com.example.model.SubscriptionPlan
import com.example.model.CreatorMasterSubmission
import com.example.model.CreatorPayoutRecord

object TamilSongCatalog {

  // Hotlinked image assets from user's design reference
  const val COVER_CYBERPULSE = "https://lh3.googleusercontent.com/aida-public/AB6AXuDYxtDMDlbLDNlev_RQNcbuZI1oaAwyN_dpDKB6Fm_4rPKxn8aHkPTx_vvl2KRh93Xwcz93OCK0_XRehq82bcoBzYBA82DXh3jrBz5HXCivi4p5SuJ3Zvkb_qJDO-LTWwDP9ggjiIb1muilRLIzlkVsMLVOSDGhHs9sh-rMoKQzA08zQNi9S8p5kcojXfzAiQdGyyMsvUjMk_JS3Po1-XByKb6iBAMKQt9XY94bvZ5Ci2oDtQj3ItWt"
  const val COVER_MIDNIGHT_HORIZON = "https://lh3.googleusercontent.com/aida-public/AB6AXuBYf_UYsJiWGudsrR_pyJetnpKSse_oZcKfGcIrmgtFEbv2E1DL6n5Vb4zC5uA6mSTUwa87VxUEjMMaSx34vs8e7NbL42jeZ9GHr-MedMKOUmY-HGoXopb642oRePy21U5l4YsexxRIu4bNFuZsEN-AurSF9lofRTb7gIH7c3SfbLD_CzrikZ_V6m2Arp7NL4T6JwbVcKaQszKcVn8mYCppqxCLoHlkAPjeOZpy8YImHrlL3A7HX9xK"
  const val COVER_MIDNIGHT_ECHOES = "https://lh3.googleusercontent.com/aida-public/AB6AXuAmLKTTDcBBnbHe8AYIz_kIFgpIVYNkt9nu4x8iNzaJj_iiXnSnOcmF-44gL3Ihtp6Ha18v523JX1V9Cbz1lfDPdpo9tQpfjWYidPHpR42PsRHW489hQS8OSKV5mjWMg-GUiQa2Km-Zx85nXGTrQ6TXECNwaTUT3ygcQqZYs8xOFd_7Iwh-E0e_0GuP_5sE6tUTLUO9QtKGBS5Hozu7LsatmBrSqwgqVWu0454n_BN09olWKcyIw-NW"
  const val COVER_VAPOR_TRAILS = "https://lh3.googleusercontent.com/aida-public/AB6AXuBkDRcOaYXHxkm_rHy6eC3r209Azozr1krxMYhTULRa5obDgKlmJNomea6rksJvBS1QItXWX0fn58DMSdDiP9rewgMmhWK-Lm9pCf09tDc-EvTBad3_fnB4eg3udjNiecAQOh-sQxfQfRBWzofY8A274_5rKXpVX3ROzAIzQDFG0LLgHWRABP9BBVfTf2oFRb5zHGUWjv6vHt1X6H58gXu62sLUvsnZF6Mk7pNjClZqJtLwsNEDyQt0"
  const val COVER_HYPERDRIVE = "https://lh3.googleusercontent.com/aida-public/AB6AXuDUOWnrw2wa-E5URAzFiCXMHQK70XIpvhBnli7IpA8ychB0OvTelE83tv5N5L68mqzfzRjpkdP6_0B_7p1kI3HQyiyrbo6VIe2iyg170aFySZISXwhO2N6aqPklXZ7gNKYzXG9Fs4CJ2tZ9xLaFRf_Ine8iy6C8HZ8KpIvHjFoCx6fbhO14OsJYqZm77oW8eS2R45QXj_9n3eF8GrJOo6SKtNY9dWFI3yVSrW8NXl1qoBF85cXvAljS"
  const val COVER_SOLARIS = "https://lh3.googleusercontent.com/aida-public/AB6AXuB56bCZlTyADp8vjcn3HmE-unxzw0v39ly-eedFHqB1hOHWAK27GBc1Ke8-e78rZ4GIlDL5vJ3ajg0MfY4McKhgd3uDFBRMzOI4WfAAbUaNgvt7WYKSbu6y3HBGFNlW5v9qB1lpL4qhvaFl0hVmbz8HcJ98jAqhI5mWF2tHLU1xBA54e9jrw3IT8lbOEcvg_A-tSDdeULMe4AzIfdau0tStX-XkCOcMbRuGmQNfFgQGbp-z02T1mihX"
  const val COVER_SUBTERRANEAN = "https://lh3.googleusercontent.com/aida-public/AB6AXuBREqi1i3U5G6ndJ53ekJElhYEA53bfcAVhIMY3I6uhws_NXIYqyZVtYgBYSPdEQZnZerzczHJp4C4CgWxVNEnfDYacmPsbmHH2_7_-37EKpghVADuYNm-OQbJI0kjWVdVlZyAmBArgtmlqXge6NJKoxf-YFysyOgXxTkdwYqXdpV_Ud8whQgydXMNYjYDGYY7rTCCc8d4HC8s5y0oi2LX_9LL0CTGlPJkmFz4JRoo3NFCgwFoNCH51"
  const val COVER_LUCID_PRISM = "https://lh3.googleusercontent.com/aida-public/AB6AXuDUwZLMy3KT-yOffxbVLiXBea_Gqt02_J8Ke3qHs7tIKy2R8qTmcfd4Q73NHMKQOTTzAYGfsNAW7Fkh5WBK9SS4FvJimBSCuU4Ev54kGuSK-OCTF1879pKZCs1Ncxa3nhFm4jrlBk0ehqd1ST8AF3qznUbq_4kDpaG5JACu5YBcMBA10-UNLPg6EIOQpPleBZXHpcev3Si7Gx8uvv7ZjnXNSQU1hGEj_ihVbPoWFFH0EZ0KE8VTmog0"
  const val COVER_GHOST_ARCHITECT = "https://lh3.googleusercontent.com/aida-public/AB6AXuD9mbMDWEBPjssVdqJZicg95ud8B5J0IFhH6e1MgBSxYgZln00fh9yuGij1wx5xcg_6pKufuZWJ3hErPa0-ugxLWLcVllnflCSGO4YWnUEkfCrhguiX_RloOaLxcrAwaPNFhEmRPs0_kvYxOOYIEfpMdpDUoXkWg4y_kghSc8O1Ks3QM-2hfRhS4nXwIaCU9cNdp4lgE9BbK2E5DfpK_LYIjCXjtwQKdmqdWxTQX_x2F_3lSFK2hRW3"
  const val COVER_HEAVY_PETALS = "https://lh3.googleusercontent.com/aida-public/AB6AXuBNWO1-V34NnjcD3uMa3tLXupFIMk458p71LokdOCG7GYsQDZ8FyBaxZ7g13fsguckOsY0YKWnZrgAPdoUKrgTllzTAQOSUhvhrz4YVcCikQNh6ANdgOQiQnON2yWiBNTLvwsr4MxncpEzf0-GrUQ5gXungs6aZliiXI7Ag7zbwU-kOTEhJhdJ6SIOeCTTNWJ0awmeyS8lbVYlSS_vzWXL8d0BGi6dP9ANfbI4A1UpB-JxGBCYFJkL_"
  const val COVER_ANALOG_OVERDRIVE = "https://lh3.googleusercontent.com/aida-public/AB6AXuD8ANv1F7PV_tNlbY5H99QzOurBv3cEeOC0NpeBm81NHu6O5xmPwAkJzte2x3W9zf9Xz6PO8GSHZpugGraT-_MBTRCY0BxsKcIzIi1cX1t21XSGzHDl6CR9xIIvCcHM5GWhJ29FiOnOVNAShBzFJHoV50E2nL_FaI1yE1q_2zo7HdwHxRHrhmHh0MyWzgVMxLo_UDlOPzoP0yeuMs9I5gF5AIOXaYf6Jo0sg9t_DUh2n9lMyDipaJC7"
  const val COVER_BANGALORE_PULSE = "https://lh3.googleusercontent.com/aida-public/AB6AXuBv2w1Iw1aiGCzutJ6ZgA4QZ3sfnY9bfasjcGjyLM0spjmzf8uh4iBgLEJbJ-WU_aoJhYiZr3T1bHKhOQEWqy0KY0PoiVHbrmbUdG0Z5dUON3RUglxmpXoMPYIEH3k3ONKWr3ejocWu-5EHe6d1sM404yjpgSqaq-XnAzse_x42NsWqMLjadDrVyKXk2kvlMfBizvWdAmddKB-4gVAKzu695K5qIb6v9N6jVc7M2MTD92PaCVToLmD6"
  const val COVER_NEON_MONSOONS = "https://lh3.googleusercontent.com/aida-public/AB6AXuDygzzPJOUAcNp-4eEsHyZrBepcrAs9VEKfuTgqs4H8DQlyOfe18_uCGt_8Thb_EGqBTNx409n4aXe-w-VaeqUBfBeYvv8lIUyTdbLprIICbY2G5IF3JvD0M6qnSuCvmbJSHj323ihMvlrASfvN5KhgAd72BSU0NFrembkekwtKDpQ1MDl61tMo3duObdpbSdWJDO-d5n-jsBd_Yc_dyBXBJksKvXqRP70_suSOKacMzmNPXTuNUBm8"

  // Songs Catalog (Cleared as requested)
  val allSongs: List<Song> = emptyList()

  // Curated Daily Mixes (Cleared as requested)
  val dailyMixes: List<DailyMix> = emptyList()

  // Subscription Plans matching Screenshot 4 & 8
  val subscriptionPlans: List<SubscriptionPlan> = listOf(
    SubscriptionPlan(
      id = "tier_starter",
      name = "Starter Tier",
      tierBadge = "ENTRY LOSSLESS",
      priceInr = 30,
      priceUsd = 0.99,
      maxConcurrentDevices = 2,
      audioQuality = "Bit-Perfect 24-bit/96kHz FLAC",
      description = "Uncompressed bit-perfect 24-bit/96kHz audio streaming for solo audiophiles.",
      activeUsers = "42,100",
      mrrContribution = "22.8% volume share",
      isTopSelected = false,
      stripePriceId = "price_starter_inr_30"
    ),
    SubscriptionPlan(
      id = "tier_duo_spatial",
      name = "Duo+ Spatial Suite",
      tierBadge = "TOP SELECTED PLAN",
      priceInr = 50,
      priceUsd = 1.99,
      maxConcurrentDevices = 3,
      audioQuality = "Lossless FLAC + Dolby Atmos 3D",
      description = "Lossless FLAC + Dolby Atmos 3D immersive room binaural engine across 3 synchronized endpoints.",
      activeUsers = "98,400",
      mrrContribution = "₹49,20,000 MRR Yield",
      isTopSelected = true,
      stripePriceId = "price_duo_spatial_inr_50"
    ),
    SubscriptionPlan(
      id = "tier_family_max",
      name = "Family Max Pack",
      tierBadge = "MASTER FLEET",
      priceInr = 100,
      priceUsd = 3.99,
      maxConcurrentDevices = 8,
      audioQuality = "Studio Master Bitstream 192kHz",
      description = "Full studio audio master bitstream, multi-room low-jitter streaming & 8 active hardware seats.",
      activeUsers = "44,420",
      mrrContribution = "₹44,42,000 MRR Contribution",
      isTopSelected = false,
      stripePriceId = "price_family_max_inr_100"
    )
  )

  // Creator Master Ingest Submissions (Cleared as requested)
  val masterSubmissions: List<CreatorMasterSubmission> = emptyList()

  // Real-Time Payout Records matching Screenshot 8
  val payoutRecords: List<CreatorPayoutRecord> = listOf(
    CreatorPayoutRecord(
      batchId = "#BATCH-88219",
      utr = "429188001923",
      recipient = "Aura Violet (CR-09418)",
      vpaOrNeft = "auraviolet@okhdfcbank",
      streamsFormatted = "418,200 (96kHz)",
      fidelityMultiplier = "1.82x",
      amountInrFormatted = "₹1,42,800.00",
      status = "SETTLED",
      timestamp = "12:44:02 IST"
    ),
    CreatorPayoutRecord(
      batchId = "#BATCH-88218",
      utr = "429188001890",
      recipient = "Shiva Kapoor (CR-08112)",
      vpaOrNeft = "shivalabs@icici",
      streamsFormatted = "620,950 (Atmos 7.1)",
      fidelityMultiplier = "2.40x",
      amountInrFormatted = "₹2,88,420.00",
      status = "SETTLED",
      timestamp = "12:41:19 IST"
    ),
    CreatorPayoutRecord(
      batchId = "#BATCH-88217",
      utr = "429188001744",
      recipient = "Nocturne Records India",
      vpaOrNeft = "NEFT: KKBK00019283",
      streamsFormatted = "1,940,110 (Mixed)",
      fidelityMultiplier = "1.65x",
      amountInrFormatted = "₹7,45,900.00",
      status = "SETTLED",
      timestamp = "12:35:40 IST"
    ),
    CreatorPayoutRecord(
      batchId = "#BATCH-88216",
      utr = "LOCK REQ: 9821",
      recipient = "Eastern Sound collective",
      vpaOrNeft = "easterncollective@axisbank",
      streamsFormatted = "94,200 (Lossless 1.0x)",
      fidelityMultiplier = "1.00x",
      amountInrFormatted = "₹36,150.00",
      status = "IN CLEARING",
      timestamp = "12:28:11 IST"
    )
  )
}
