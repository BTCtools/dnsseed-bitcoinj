package com.mattcorallo.bitcoinjdnsseed;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

import org.bitcoinj.core.Sha256Hash;

/**
 * Copyright 2012 Matt Corallo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


public abstract class DataStore {
    public static enum PeerState {
        // UNTESTED MUST be first
        UNTESTED,
        LOW_BLOCK_COUNT,
        HIGH_BLOCK_COUNT,
        LOW_VERSION,
        PEER_DISCONNECTED,
        NOT_FULL_NODE,
        TIMEOUT,
        TIMEOUT_DURING_REQUEST,
        GOOD,
        WAS_GOOD, // Was good up until some time N, now its not
        UNTESTABLE_ADDRESS
    }
    // The maximum length of a name in PeerState
    public static final int PEER_STATE_MAX_LENGTH = 22;
    
    // Retry times in seconds
    public final Object retryTimesLock = new Object();
    public int[] retryTimes = new int[PeerState.values().length];
    // Locked by retryTimesLock
    // If the node was GOOD within the last N minutes, retry as often as GOOD
    public int ageOfLastSuccessToRetryAsGood;
    

    // Timeout is measured from initial connect attempt until a single block has been fully received (in seconds)
    public final Object totalRunTimeoutLock = new Object();
    public int totalRunTimeout = 10;
    
    // New connection opened per second
    public final Object connectionsPerSecondLock = new Object();
    public int connectionsPerSecond = 5;
    
    public final Object minVersionLock = new Object();
    public int minVersion = 70002;

    public DataStore() {
        synchronized(retryTimesLock) {
            retryTimes[PeerState.UNTESTED.ordinal()] =                0 *60*60; // Always try UNTESTED Nodes
            retryTimes[PeerState.LOW_BLOCK_COUNT.ordinal()] =            90*60;
            retryTimes[PeerState.HIGH_BLOCK_COUNT.ordinal()] =        2 *60*60;
            retryTimes[PeerState.LOW_VERSION.ordinal()] =            24 *60*60;
            retryTimes[PeerState.PEER_DISCONNECTED.ordinal()] =      48 *60*60;
            retryTimes[PeerState.NOT_FULL_NODE.ordinal()] =          24 *60*60;
            retryTimes[PeerState.TIMEOUT.ordinal()] =                48 *60*60;
            retryTimes[PeerState.TIMEOUT_DURING_REQUEST.ordinal()] =  1 *60*60;
            retryTimes[PeerState.GOOD.ordinal()] =                       30*60;
            retryTimes[PeerState.WAS_GOOD.ordinal()] =                   45*60;
            ageOfLastSuccessToRetryAsGood =                          24 *60*60;

            retryTimes[PeerState.UNTESTABLE_ADDRESS.ordinal()] =      Integer.MAX_VALUE; // Never retest
        }
    }
    
    public abstract void addUpdateNode(InetSocketAddress addr, PeerState state);
    
    public abstract List<InetSocketAddress> getNodesToTest();
    
    public abstract List<InetAddress> getMostRecentGoodNodes(int numNodes, int port);

    public abstract String getStatus();
}