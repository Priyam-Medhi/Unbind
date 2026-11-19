const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_unbind_123";
const BOB_UID = "bob_unbind_456";
const SPACE_ID = "space_doomscrolling_1";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastDate(msAgo = 5000) {
  return new Date(Date.now() - msAgo);
}

function validSpacePayload(uid, ts = pastDate(10000)) {
  return {
    userId: uid,
    title: "Smartphone Doomscrolling",
    archetype: "DOOMSCROLLING",
    iconKey: "phone",
    dailyCostUsd: 10.0,
    dailyHoursSaved: 2.0,
    currentStreakDays: 0,
    longestStreakDays: 0,
    totalCleanDays: 0,
    claimedBudgetUsd: 0.0,
    lastCheckInDate: "",
    lastCheckInOutcome: "NONE",
    ritualStatusNote: "Sanctuary initialized",
    createdAt: ts,
    updatedAt: ts,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write any space or subcollection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("spaces").get());
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("spaces")
      .doc(SPACE_ID)
      .set(validSpacePayload(ALICE_UID))
  );
});

test("Owner can create, list, and partially update HabitSpace; ghost fields and future timestamps fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const spacesCol = aliceDb.collection("users").doc(ALICE_UID).collection("spaces");
  const spaceRef = spacesCol.doc(SPACE_ID);

  // Empty list query succeeds
  await assertSucceeds(spacesCol.get());

  // Valid create (no @DocumentId `id` field)
  await assertSucceeds(spaceRef.set(validSpacePayload(ALICE_UID)));

  // Action 1: Check-in streak update
  await assertSucceeds(
    spaceRef.update({
      currentStreakDays: 1,
      longestStreakDays: 1,
      totalCleanDays: 1,
      lastCheckInDate: "2026-11-18",
      lastCheckInOutcome: "CLEAN",
      ritualStatusNote: "Kept free today",
      updatedAt: pastDate(1000),
    })
  );

  // Action 2: Valuation settings update
  await assertSucceeds(
    spaceRef.update({
      dailyCostUsd: 12.5,
      dailyHoursSaved: 2.5,
      updatedAt: pastDate(500),
    })
  );

  // Action 3: Claim reward budget update
  await assertSucceeds(
    spaceRef.update({
      claimedBudgetUsd: 10.0,
      updatedAt: pastDate(200),
    })
  );

  // Reject ghost field / @DocumentId `id` on create
  await assertFails(
    spacesCol.doc("space_ghost").set({
      ...validSpacePayload(ALICE_UID),
      id: "space_ghost",
    })
  );

  // Reject immutable createdAt modification
  await assertFails(
    spaceRef.update({
      createdAt: pastDate(100),
      updatedAt: pastDate(100),
    })
  );

  // Reject future timestamp
  await assertFails(
    spacesCol.doc("space_future").set(validSpacePayload(ALICE_UID, new Date(Date.now() + 60000)))
  );
});

test("Cross-user isolation blocks Bob from reading or modifying Alice's space", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("spaces")
      .doc(SPACE_ID)
      .set(validSpacePayload(ALICE_UID))
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("spaces").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("spaces").doc(SPACE_ID).get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("spaces").doc(SPACE_ID).update({
      title: "Hijacked",
      updatedAt: pastDate(500),
    })
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("spaces").doc(SPACE_ID).delete()
  );
});

test("CheckInLog, GrowthTask, RewardGoal (with terminal lock), and SavedInsight follow schema and parent gate", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const spaceRef = aliceDb.collection("users").doc(ALICE_UID).collection("spaces").doc(SPACE_ID);

  // Orphaned subcollection write without parent space fails
  await assertFails(
    spaceRef.collection("tasks").doc("task_orphan").set({
      userId: ALICE_UID,
      spaceId: SPACE_ID,
      title: "Orphan task",
      subtitle: "",
      category: "TODO",
      iconKey: "book",
      completedDate: "",
      sortOrder: 0,
      createdAt: pastDate(2000),
      updatedAt: pastDate(2000),
    })
  );

  // Create parent space first
  await assertSucceeds(spaceRef.set(validSpacePayload(ALICE_UID)));

  // 1. CheckInLog create & update
  const checkInRef = spaceRef.collection("checkIns").doc("2026-11-18");
  await assertSucceeds(
    checkInRef.set({
      userId: ALICE_UID,
      spaceId: SPACE_ID,
      dateKey: "2026-11-18",
      outcome: "CLEAN",
      urgeLevel: 2,
      peakUrgeWindow: "9:30 PM - 11:00 PM",
      reflectionNote: "Read before bed instead of scrolling.",
      createdAt: pastDate(3000),
      updatedAt: pastDate(3000),
    })
  );
  await assertSucceeds(spaceRef.collection("checkIns").get());
  await assertSucceeds(
    checkInRef.update({
      outcome: "MILD_URGE",
      urgeLevel: 4,
      reflectionNote: "Felt an urge at 10 PM but resisted.",
      updatedAt: pastDate(1000),
    })
  );

  // 2. GrowthTask create, complete toggle, reclassify
  const taskRef = spaceRef.collection("tasks").doc("task_1");
  await assertSucceeds(
    taskRef.set({
      userId: ALICE_UID,
      spaceId: SPACE_ID,
      title: "Morning 15-min sunlight walk",
      subtitle: "Circadian anchor",
      category: "NON_NEGOTIABLE",
      iconKey: "sun",
      completedDate: "",
      sortOrder: 0,
      createdAt: pastDate(3000),
      updatedAt: pastDate(3000),
    })
  );
  await assertSucceeds(spaceRef.collection("tasks").get());
  await assertSucceeds(
    taskRef.update({
      completedDate: "2026-11-18",
      updatedAt: pastDate(1000),
    })
  );
  await assertSucceeds(
    taskRef.update({
      category: "TODO",
      updatedAt: pastDate(500),
    })
  );

  // 3. RewardGoal create, redeem, and terminal lock enforcement
  const rewardRef = spaceRef.collection("rewards").doc("reward_1");
  await assertSucceeds(
    rewardRef.set({
      userId: ALICE_UID,
      spaceId: SPACE_ID,
      title: "Specialty Artisan Dinner",
      subtitle: "Milestone: 14 Days Clean",
      targetDays: 14,
      budgetUsd: 60.0,
      coverPresetKey: "dinner",
      redeemed: false,
      createdAt: pastDate(3000),
      updatedAt: pastDate(3000),
    })
  );
  await assertSucceeds(spaceRef.collection("rewards").get());
  // Redeem transition succeeds
  await assertSucceeds(
    rewardRef.update({
      redeemed: true,
      redeemedAt: pastDate(1000),
      updatedAt: pastDate(1000),
    })
  );
  // Terminal state lock rejects un-redeeming or editing a redeemed reward
  await assertFails(
    rewardRef.update({
      redeemed: false,
      updatedAt: pastDate(500),
    })
  );
  await assertFails(
    rewardRef.update({
      title: "Edited After Redeem",
      updatedAt: pastDate(500),
    })
  );

  // 4. SavedInsight create & bookmark toggle
  const insightRef = spaceRef.collection("savedInsights").doc("insight_1");
  await assertSucceeds(
    insightRef.set({
      userId: ALICE_UID,
      spaceId: SPACE_ID,
      quote: "You do not rise to the level of your goals. You fall to the level of your systems.",
      author: "James Clear",
      source: "Atomic Habits",
      reflectionNote: "",
      bookmarked: true,
      createdAt: pastDate(2000),
      updatedAt: pastDate(2000),
    })
  );
  await assertSucceeds(spaceRef.collection("savedInsights").get());
  await assertSucceeds(
    insightRef.update({
      reflectionNote: "Build friction before bedtime.",
      bookmarked: true,
      updatedAt: pastDate(500),
    })
  );
});
